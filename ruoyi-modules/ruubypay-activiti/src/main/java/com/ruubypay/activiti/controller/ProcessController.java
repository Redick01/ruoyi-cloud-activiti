package com.ruubypay.activiti.controller;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
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

    /**
     * 审批历史列表
     */
    @PostMapping("/listHistory")
    @ResponseBody
    public TableDataInfo listHistory( @RequestBody HistoricActivity historicActivity ) {
        startPage();
        List<HistoricActivity> list = processService.selectHistoryList(historicActivity);
        return getDataTable(list);
    }

    /**
     * 进度查看
     */
    @RequestMapping(value = "/read-resource")
    public void readResource( String pProcessInstanceId, HttpServletResponse response )
            throws Exception {
        // 设置页面不缓存
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
            // 通过接口读取
            InputStream resourceAsStream = repositoryService
                    .getResourceAsStream(pd.getDeploymentId(), resourceName);

            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
        }
    }

    /**
     * 转办
     */
    @PostMapping("/delegate")
    @ResponseBody
    public AjaxResult delegate( String taskId, String delegateToUser ) {
        processService.delegate(taskId, SecurityUtils.getUsername(), delegateToUser);
        return success();
    }

    /**
     * 撤销流程实例
     */
    @PostMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply( String instanceId ) {
        processService.cancelApply(instanceId, "用户撤销");
        return success();
    }

    /**
     * 激活/挂起流程实例
     */
    @PostMapping("/suspendOrActiveApply")
    @ResponseBody
    public AjaxResult suspendOrActiveApply( String instanceId, String suspendState ) {
        processService.suspendOrActiveApply(instanceId, suspendState);
        return success();
    }

    /**
     * 我的待办列表
     */
    @GetMapping("/taskList")
    @ResponseBody
    public TableDataInfo taskList(TaskVo taskVo) {
        return processService.findTodoTasks(taskVo);
    }

    /**
     * 办理任务
     */
    @PostMapping("/complete")
    @ResponseBody
    public AjaxResult complete( String taskId, String instanceId, String variables ) {
        processService.complete(taskId, instanceId, variables);
        return success();
    }

    /**
     * 我的已办列表
     */
    @GetMapping("/taskDoneList")
    @ResponseBody
    public TableDataInfo taskDoneList(TaskVo taskVo) {
        return processService.findDoneTasks(taskVo);
    }

    private List<String> getHighLightedFlows( BpmnModel bpmnModel,
            List<HistoricActivityInstance> historicActivityInstances ) {
        //24小时制
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 用以保存高亮的线flowId
        List<String> highFlows = new ArrayList<String>();

        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 对历史流程节点进行遍历
            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess()
                    .getFlowElement(historicActivityInstances.get(i).getActivityId());
            // 用以保存后续开始时间相同的节点
            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();
            FlowNode sameActivityImpl1 = null;
            // 第一个节点
            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);
            HistoricActivityInstance activityImp2_;

            for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
                // 后续第1个节点
                activityImp2_ = historicActivityInstances.get(k);
                //都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
                if (activityImpl_.getActivityType().equals("userTask") && activityImp2_
                        .getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_
                                .getStartTime())))
                {

                } else {
                    sameActivityImpl1 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(
                            //找到紧跟在后面的一个节点
                            historicActivityInstances.get(k).getActivityId());
                    break;
                }

            }
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);
                // 如果第一个节点和第二个节点开始时间相同保存
                if (df.format(activityImpl1.getStartTime())
                        .equals(df.format(activityImpl2.getStartTime()))) {
                    FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess()
                            .getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            // 取出节点的所有出去的线
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
            // 对所有的线进行遍历
            for (SequenceFlow pvmTransition : pvmTransitions) {
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(
                        // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                        pvmTransition.getTargetRef());
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    /**
     * 获取流程图像，已执行节点和流程线高亮显示
     */
    private void getActivitiProccessImage( String pProcessInstanceId,
            HttpServletResponse response ) {
        logger.info("[开始]-获取流程图图像");
        try {
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(pProcessInstanceId).singleResult();

            if (historicProcessInstance == null) {
                //throw new BusinessException("获取流程实例ID[" + pProcessInstanceId + "]对应的历史流程实例失败！");
            } else {
                // 获取流程定义
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(
                                historicProcessInstance.getProcessDefinitionId());

                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
                List<HistoricActivityInstance> historicActivityInstanceList = historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId()
                        .asc().list();

                // 已执行的节点ID集合
                List<String> executedActivityIdList = new ArrayList<String>();
                logger.info("获取已经执行的节点ID");
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());
                }

                BpmnModel bpmnModel = repositoryService
                        .getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                // 已执行的线集合
                List<String> flowIds = new ArrayList<String>();
                // 获取流程走过的线 (getHighLightedFlows是下面的方法)
                flowIds = getHighLightedFlows(bpmnModel,
                        historicActivityInstanceList);

//                // 获取流程图图像字符流
//                ProcessDiagramGenerator pec = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
//                //配置字体
//                InputStream imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds,"宋体","微软雅黑","黑体",null,2.0);

                Set<String> currIds = runtimeService.createExecutionQuery()
                        .processInstanceId(pProcessInstanceId).list()
                        .stream().map(e -> e.getActivityId()).collect(Collectors.toSet());

                ICustomProcessDiagramGenerator diagramGenerator = (ICustomProcessDiagramGenerator) processEngine
                        .getProcessEngineConfiguration().getProcessDiagramGenerator();
                InputStream imageStream = diagramGenerator
                        .generateDiagram(bpmnModel, "png", executedActivityIdList,
                                flowIds, "宋体", "宋体", "宋体", null, 1.0,
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
            logger.info("[完成]-获取流程图图像");
        } catch (Exception e) {
            logger.error("【异常】-获取流程图失败！" + e.getMessage());
            throw new CustomException("获取流程图失败！" + e.getMessage());
        }
    }
}
