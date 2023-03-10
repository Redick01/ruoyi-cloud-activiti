package com.ruubypay.activiti.controller;

import com.ruoyi.activiti.api.domain.ProcessInstanceStartRequest;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.CustomException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruubypay.activiti.configure.ICustomProcessDiagramGenerator;
import com.ruubypay.activiti.constants.WorkflowConstants;
import com.ruubypay.activiti.domain.HistoricActivity;
import com.ruubypay.activiti.domain.TaskVo;
import com.ruubypay.activiti.service.IProcessService;
import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick
 */
@RestController
@RequestMapping("/process")
@AllArgsConstructor
public class ProcessController extends BaseController {

    private final RepositoryService repositoryService;

    private final HistoryService historyService;

    private final ProcessEngine processEngine;

    private final IProcessService processService;

    private final RuntimeService runtimeService;

    private final IdentityService identityService;

    @PostMapping("/startProcessInstance")
    public @ResponseBody R<ProcessInstanceResponse> startProcessInstance(@RequestBody ProcessInstanceStartRequest request) {
        // ?????????????????????????????????ID???????????????????????????ID?????????activiti:initiator???
        identityService.setAuthenticatedUserId(request.getUsername());
        // ??????????????????????????? key
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(request.getProcessDefinitionKey()
                , request.getBusinessKey(), new HashMap<>());
        ProcessInstanceResponse response = new ProcessInstanceResponse();
        response.setId(instance.getId());
        response.setBusinessKey(instance.getBusinessKey());
        response.setProcessDefinitionId(instance.getProcessDefinitionId());
        response.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        response.setName(instance.getName());
        return R.ok(response);
    }

    /**
     * ??????????????????
     */
    @PostMapping("/listHistory")
    @ResponseBody
    public TableDataInfo listHistory( @RequestBody HistoricActivity historicActivity ) {
        startPage();
        List<HistoricActivity> list = processService.selectHistoryList(historicActivity);
        return getDataTable(list);
    }

    /**
     * ????????????
     */
    @RequestMapping(value = "/read-resource")
    public void readResource(String pProcessInstanceId, HttpServletResponse response)
            throws Exception {
        // ?????????????????????
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        String processDefinitionId = "";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(pProcessInstanceId).singleResult();
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService
                    .createHistoricProcessInstanceQuery().processInstanceId(pProcessInstanceId)
                    .singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }
        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition pd = pdq.processDefinitionId(processDefinitionId).singleResult();

        String resourceName = pd.getDiagramResourceName();

        if (resourceName.endsWith(".png") && !StringUtils.isEmpty(pProcessInstanceId)) {
            getActivitiProccessImage(pProcessInstanceId, response);
        } else {
            // ??????????????????
            InputStream resourceAsStream = repositoryService
                    .getResourceAsStream(pd.getDeploymentId(), resourceName);

            // ?????????????????????????????????
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        }
    }

    /**
     * ??????
     */
    @PostMapping("/delegate")
    @ResponseBody
    public AjaxResult delegate( String taskId, String delegateToUser ) {
        processService.delegate(taskId, SecurityUtils.getUsername(), delegateToUser);
        return success();
    }

    /**
     * ??????????????????
     */
    @PostMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply( String instanceId ) {
        processService.cancelApply(instanceId, "????????????");
        return success();
    }

    /**
     * ??????/??????????????????
     */
    @PostMapping("/suspendOrActiveApply")
    @ResponseBody
    public AjaxResult suspendOrActiveApply( String instanceId, String suspendState ) {
        processService.suspendOrActiveApply(instanceId, suspendState);
        return success();
    }

    /**
     * ??????????????????
     */
    @GetMapping("/taskList")
    @ResponseBody
    public TableDataInfo taskList(TaskVo taskVo) {
        return processService.findTodoTasks(taskVo);
    }

    /**
     * ????????????
     */
    @PostMapping("/complete")
    @ResponseBody
    public AjaxResult complete( String taskId, String instanceId, String variables ) {
        processService.complete(taskId, instanceId, variables);
        return success();
    }

    /**
     * ??????????????????
     */
    @GetMapping("/taskDoneList")
    @ResponseBody
    public TableDataInfo taskDoneList(TaskVo taskVo) {
        return processService.findDoneTasks(taskVo);
    }

    private List<String> getHighLightedFlows( BpmnModel bpmnModel,
            List<HistoricActivityInstance> historicActivityInstances ) {
        //24?????????
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // ????????????????????????flowId
        List<String> highFlows = new ArrayList<String>();

        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // ?????????????????????????????????
            // ?????????????????????????????????
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess()
                    .getFlowElement(historicActivityInstances.get(i).getActivityId());
            // ?????????????????????????????????????????????
            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();
            FlowNode sameActivityImpl1 = null;
            // ???????????????
            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);
            HistoricActivityInstance activityImp2_;

            for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
                // ?????????1?????????
                activityImp2_ = historicActivityInstances.get(k);
                //??????usertask???????????????????????????????????????????????????????????????????????????????????????
                if (activityImpl_.getActivityType().equals("userTask") && activityImp2_
                        .getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_
                                .getStartTime())))
                {

                } else {
                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(
                            //????????????????????????????????????
                            historicActivityInstances.get(k).getActivityId());
                    break;
                }

            }
            // ????????????????????????????????????????????????????????????
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);
                // ???????????????????????????????????????????????????????????????
                if (df.format(activityImpl1.getStartTime())
                        .equals(df.format(activityImpl2.getStartTime()))) {
                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess()
                            .getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // ????????????????????????
                    break;
                }
            }
            // ?????????????????????????????????
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
            // ???????????????????????????
            for (SequenceFlow pvmTransition : pvmTransitions) {
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(
                        // ?????????????????????????????????????????????????????????????????????????????????id?????????????????????
                        pvmTransition.getTargetRef());
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    /**
     * ????????????????????????????????????????????????????????????
     */
    private void getActivitiProccessImage( String pProcessInstanceId,
            HttpServletResponse response ) {
        logger.info("[??????]-?????????????????????");
        try {
            //  ????????????????????????
            HistoricProcessInstance historicProcessInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(pProcessInstanceId).singleResult();

            if (historicProcessInstance == null) {
                //throw new BusinessException("??????????????????ID[" + pProcessInstanceId + "]????????????????????????????????????");
            } else {
                // ??????????????????
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(
                                historicProcessInstance.getProcessDefinitionId());

                // ??????????????????????????????????????????????????????????????????????????????????????????
                List<HistoricActivityInstance> historicActivityInstanceList = historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId()
                        .asc().list();

                // ??????????????????ID??????
                List<String> executedActivityIdList = new ArrayList<String>();
                logger.info("???????????????????????????ID");
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());
                }

                BpmnModel bpmnModel = repositoryService
                        .getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                // ?????????????????????
                List<String> flowIds = new ArrayList<String>();
                // ???????????????????????? (getHighLightedFlows??????????????????)
                flowIds = getHighLightedFlows(bpmnModel,
                        historicActivityInstanceList);

//                // ??????????????????????????????
//                ProcessDiagramGenerator pec = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
//                //????????????
//                InputStream imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds,"??????","????????????","??????",null,2.0);

                Set<String> currIds = runtimeService.createExecutionQuery()
                        .processInstanceId(pProcessInstanceId).list()
                        .stream().map(e -> e.getActivityId()).collect(Collectors.toSet());

                ICustomProcessDiagramGenerator diagramGenerator = (ICustomProcessDiagramGenerator) processEngine
                        .getProcessEngineConfiguration().getProcessDiagramGenerator();
                InputStream imageStream = diagramGenerator
                        .generateDiagram(bpmnModel, "png", executedActivityIdList,
                                flowIds, "??????", "??????", "??????", null, 1.0,
                                new Color[]{WorkflowConstants.COLOR_NORMAL,
                                        WorkflowConstants.COLOR_CURRENT}, currIds);

                response.setContentType("image/png");
                OutputStream os = response.getOutputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = imageStream.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                imageStream.close();
            }
            logger.info("[??????]-?????????????????????");
        } catch (Exception e) {
            logger.error("????????????-????????????????????????" + e.getMessage());
            throw new CustomException("????????????????????????" + e.getMessage());
        }
    }
}
