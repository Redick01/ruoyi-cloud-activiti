package com.ruubypay.activiti.service.impl;

import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.constant.HttpStatus;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruubypay.activiti.domain.HistoricActivity;
import com.ruubypay.activiti.domain.TaskVo;
import com.ruubypay.activiti.mapper.TaskMapper;
import com.ruubypay.activiti.service.IProcessService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Redick01
 */
@Service
@AllArgsConstructor
public class ProcessServiceImpl implements IProcessService {

    protected final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    private final IdentityService identityService;

    private final TaskService taskService;

    private final HistoryService historyService;

    private final RuntimeService runtimeService;

    private final RemoteUserService remoteUserService;

    private final TaskMapper taskMapper;

    @Override
    public List<HistoricActivity> selectHistoryList(HistoricActivity historicActivity) {
        List<HistoricActivity> activityList = new ArrayList<>();
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
        if (StringUtils.isNotBlank(historicActivity.getAssignee())) {
            query.taskAssignee(historicActivity.getAssignee());
        }
        if (StringUtils.isNotBlank(historicActivity.getActivityName())) {
            query.activityName(historicActivity.getActivityName());
        }
        List<HistoricActivityInstance> list = query
                .processInstanceId(historicActivity.getProcessInstanceId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        list.forEach(instance -> {
            HistoricActivity activity = new HistoricActivity();
            BeanUtils.copyProperties(instance, activity);
            String taskId = instance.getTaskId();
            List<Comment> comment = taskService.getTaskComments(taskId, "comment");
            if (!CollectionUtils.isEmpty(comment)) {
                activity.setComment(comment.get(0).getFullMessage());
            }
            // ??????????????????deleteReason ?????? null???????????????????????????
            if (StringUtils.isNotBlank(activity.getDeleteReason())) {
                activity.setComment(activity.getDeleteReason());
            }
            SysUser sysUser = remoteUserService.getUserInfo(instance.getAssignee(),
                    SecurityConstants.INNER).getData().getSysUser();
            if (sysUser != null) {
                activity.setAssigneeName(sysUser.getNickName());
            }
            activityList.add(activity);
        });

        // ??????????????????????????????????????????
        HistoricActivity startActivity = new HistoricActivity();
        query = historyService.createHistoricActivityInstanceQuery();
        HistoricActivityInstance startActivityInstance = query
                .processInstanceId(historicActivity.getProcessInstanceId())
                .activityType("startEvent")
                .singleResult();
        BeanUtils.copyProperties(startActivityInstance, startActivity);
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(historicActivity.getProcessInstanceId())
                .singleResult();
        startActivity.setAssignee(historicProcessInstance.getStartUserId());
        SysUser sysUser = remoteUserService.getUserInfo(historicProcessInstance.getStartUserId(),
                SecurityConstants.INNER).getData().getSysUser();
        if (sysUser != null) {
            startActivity.setAssigneeName(sysUser.getNickName());
        }
        startActivity.setComment("????????????");

        // ?????????????????????????????????
        boolean necessaryAdd = true;
        if ((StringUtils.isNotBlank(historicActivity.getActivityName()) && !startActivity
                .getActivityName().equals(historicActivity.getActivityName()))
                || (StringUtils.isNotBlank(historicActivity.getAssignee()) && !startActivity
                .getAssignee().equals(historicActivity.getAssignee()))) {
            necessaryAdd = false;
        }
        if (necessaryAdd) {
            activityList.add(0, startActivity);
        }

        // ???????????????????????????????????????
        HistoricActivity endActivity = new HistoricActivity();
        query = historyService.createHistoricActivityInstanceQuery();
        HistoricActivityInstance endActivityInstance = query
                .processInstanceId(historicActivity.getProcessInstanceId())
                .activityType("endEvent")
                .singleResult();
        if (null != endActivityInstance) {
            BeanUtils.copyProperties(endActivityInstance, endActivity);
            endActivity.setAssignee("admin");
            sysUser = remoteUserService.getUserInfo("admin",
                    SecurityConstants.INNER).getData().getSysUser();
            if (sysUser != null) {
                endActivity.setAssigneeName(sysUser.getNickName());
            }
            endActivity.setComment("????????????");

            // ?????????????????????????????????
            necessaryAdd = true;
            if ((StringUtils.isNotBlank(historicActivity.getActivityName()) && !endActivity
                    .getActivityName().equals(historicActivity.getActivityName()))
                    || (StringUtils.isNotBlank(historicActivity.getAssignee()) && !endActivity
                    .getAssignee().equals(historicActivity.getAssignee()))) {
                necessaryAdd = false;
            }
            if (necessaryAdd) {
                activityList.add(endActivity);
            }
        }
        return activityList;
    }

    @Override
    public void delegate(String taskId, String fromUser, String delegateToUser) {
        taskService.delegateTask(taskId, delegateToUser);
    }

    @Override
    public void cancelApply(String instanceId, String deleteReason) {
        // ???????????????????????????????????? act_ru_task ??????????????????????????? act_hi_taskinst ????????????????????????????????????????????????finished??????
        runtimeService.deleteProcessInstance(instanceId, deleteReason);
    }

    @Override
    public void suspendOrActiveApply(String instanceId, String suspendState) {
        if ("1".equals(suspendState)) {
            // ????????????????????????????????????????????????????????????????????????id??????????????????????????????
            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????????????????
            // ???????????????act_ru_task ??? SUSPENSION_STATE_ ??? 2
            runtimeService.suspendProcessInstanceById(instanceId);
        } else if ("2".equals(suspendState)) {
            runtimeService.activateProcessInstanceById(instanceId);
        }
    }

    @Override
    public TableDataInfo findTodoTasks(TaskVo taskVo) {
        taskVo.setUserId(SecurityUtils.getUsername());
        taskVo.setOffset((taskVo.getPageNum() - 1) * taskVo.getPageSize());
        List<Map> tasks = taskMapper.findTodoList(taskVo);
        Integer count = taskMapper.findTodoCount(taskVo);
        List<TaskVo> taskVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tasks)) {
            tasks.forEach(task -> {
                TaskVo newTaskVo = new TaskVo();
                newTaskVo.setType("todo");
                newTaskVo.setUserId(SecurityUtils.getUsername());
                newTaskVo.setTaskId(task.get("ID_").toString());
                newTaskVo.setTaskName(task.get("NAME_").toString());
                newTaskVo.setInstanceId(task.get("PROC_INST_ID_").toString());
                newTaskVo.setSuspendState(task.get("SUSPENSION_STATE_").toString());
                newTaskVo.setCreateTime((Date) task.get("CREATE_TIME_"));
                if ("2".equals(newTaskVo.getSuspendState())) {
                    newTaskVo.setSuspendStateName("?????????");
                } else {
                    newTaskVo.setSuspendStateName("?????????");
                }
                String nickName = remoteUserService.getUserInfo(newTaskVo.getUserId(), SecurityConstants.INNER).getData().getSysUser().getNickName();
                newTaskVo.setAssigneeName(nickName);
                // ????????????????????????????????? map ???
                Map ibMap = taskMapper.selectInstanceBusinessByInstanceId(task.get("PROC_INST_ID_").toString());
                if (!CollectionUtils.isEmpty(ibMap)) {
                    Map<String, Object> formData = taskMapper.selectBusinessByBusinessKeyAndModule(ibMap.get("business_key").toString(), ibMap.get("module").toString());
                    if (!CollectionUtils.isEmpty(formData)) {
                        newTaskVo.setFormData(getLine2HumpMap(formData));
                    }
                }
                taskVos.add(newTaskVo);
            });
        }
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("????????????");
        rspData.setRows(taskVos);
        rspData.setTotal(count);
        return rspData;
    }

    @Override
    public TableDataInfo findDoneTasks(TaskVo taskVo) {
        taskVo.setUserId(SecurityUtils.getUsername());
        taskVo.setOffset((taskVo.getPageNum() - 1) * taskVo.getPageSize());
        List<Map> tasks = taskMapper.findDoneList(taskVo);
        Integer count = taskMapper.findDoneCount(taskVo);
        List<TaskVo> taskVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tasks)) {
            tasks.forEach(task -> {
                TaskVo newTaskVo = new TaskVo();
                newTaskVo.setType("done");
                newTaskVo.setUserId(SecurityUtils.getUsername());
                newTaskVo.setTaskId(task.get("ID_").toString());
                newTaskVo.setTaskName(task.get("NAME_").toString());
                newTaskVo.setInstanceId(task.get("PROC_INST_ID_").toString());
                newTaskVo.setAssignee(task.get("ASSIGNEE_").toString());
                newTaskVo.setStartTime((Date) task.get("START_TIME_"));
                newTaskVo.setEndTime((Date) task.get("END_TIME_"));
                String nickName = remoteUserService.getUserInfo(newTaskVo.getUserId(), SecurityConstants.INNER).getData().getSysUser().getNickName();
                newTaskVo.setAssigneeName(nickName);
                // ????????????????????????????????? map ???
                Map ibMap = taskMapper.selectInstanceBusinessByInstanceId(task.get("PROC_INST_ID_").toString());
                if (!CollectionUtils.isEmpty(ibMap)) {
                    Map<String, Object> formData = taskMapper.selectBusinessByBusinessKeyAndModule(ibMap.get("business_key").toString(), ibMap.get("module").toString());
                    if (!CollectionUtils.isEmpty(formData)) {
                        newTaskVo.setFormData(getLine2HumpMap(formData));
                    }
                }
                taskVos.add(newTaskVo);
            });
        }
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("????????????");
        rspData.setRows(taskVos);
        rspData.setTotal(count);
        return rspData;
    }

    @Override
    public void complete(String taskId, String instanceId, String variablesStr) {
        logger.info("variables: " + variablesStr);
        Map<String, Object> variables = (Map<String, Object>) JSON.parse(variablesStr);
        String comment = variables.get("comment").toString();
        String pass = variables.get("pass").toString();
        try {
            variables.put("pass", "true".equals(pass));
            // ??????????????????????????????
            // p.s. ??????????????????????????? resolved ????????????????????????
            // ????????? complete ??????????????? resolved

            // ????????????????????????????????????????????????
            TaskEntityImpl task = (TaskEntityImpl) taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();
            // DELEGATION_ ??? PENDING ??????????????????????????????
            if (task.getDelegationState() != null && task.getDelegationState().equals(
                    DelegationState.PENDING)) {
                taskService.resolveTask(taskId, variables);
                // ?????????????????????
                String delegateUserName = remoteUserService.getUserInfo(SecurityUtils.getUsername(), SecurityConstants.INNER).getData().getSysUser().getNickName();
                comment += "??????" + delegateUserName + "?????????";

                // ????????? OWNER_ ??? null ??????????????????????????????????????????????????????????????????????????????
                if (StringUtils.isBlank(task.getOwner())) {
                    taskService.claim(taskId, SecurityUtils.getUsername());
                }
            } else {
                // ?????????????????????act_hi_taskinst ?????? assignee ??????????????? null
                taskService.claim(taskId, SecurityUtils.getUsername());
            }

            if (StringUtils.isNotEmpty(comment)) {
                identityService.setAuthenticatedUserId(SecurityUtils.getUsername());
                taskService.addComment(taskId, instanceId, comment);
            }

            taskService.complete(taskId, variables);
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
        }
    }

    @Override
    public <T> void richProcessField(T entity) {
        Class<?> clazz = entity.getClass();
        try {
            Method getInstanceId = clazz.getDeclaredMethod("getInstanceId");
            String instanceId = (String) getInstanceId.invoke(entity);
            Method setTaskId = clazz.getSuperclass().getDeclaredMethod("setTaskId", String.class);
            Method setTaskName = clazz.getSuperclass().getDeclaredMethod("setTaskName", String.class);
            Method setSuspendState = clazz.getSuperclass().getDeclaredMethod("setSuspendState", String.class);
            Method setSuspendStateName = clazz.getSuperclass().getDeclaredMethod("setSuspendStateName", String.class);
            // ????????????
            if (StringUtils.isNotBlank(instanceId)) {
                List<Task> taskList = taskService.createTaskQuery()
                        .processInstanceId(instanceId)
                        .list();    // ????????????????????????????????????????????????
                if (!CollectionUtils.isEmpty(taskList)) {
                    TaskEntityImpl task = (TaskEntityImpl) taskList.get(0);
                    setTaskId.invoke(entity, task.getId());
                    if (task.getSuspensionState() == 2) {
                        setTaskName.invoke(entity, "?????????");
                        setSuspendState.invoke(entity, "2");
                        setSuspendStateName.invoke(entity, "?????????");
                    } else {
                        setTaskName.invoke(entity, task.getName());
                        setSuspendState.invoke(entity, "1");
                        setSuspendStateName.invoke(entity, "?????????");
                    }
                } else {
                    // ????????????????????????
                    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                            .processInstanceId(instanceId)
                            .orderByTaskCreateTime()
                            .desc()
                            .list();
                    if (!CollectionUtils.isEmpty(list)) {
                        // ?????????????????????????????????
                        HistoricTaskInstance lastTask = list.get(0);
                        if (StringUtils.isNotBlank(lastTask.getDeleteReason())) {
                            setTaskName.invoke(entity, "?????????");
                        } else {
                            setTaskName.invoke(entity, "?????????");
                        }
                        // ??????????????????????????????id???????????????-1
                        setTaskId.invoke(entity, "-1");
                    } else {
                        // ????????????????????????????????????????????????instanceId?????????????????????
                        setTaskName.invoke(entity, "???????????????");
                        // ?????????????????????????????????????????????????????????
                        setTaskId.invoke(entity, "-2");
                    }
                }
            } else {
                setTaskName.invoke(entity, "?????????");
            }
        } catch (Exception e) {
            logger.error("????????????????????????", e);
            e.printStackTrace();
        }
    }

    private Map<String, Object> getLine2HumpMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // key ?????????????????? apply_user_id ????????? applyUserId
            key = lineToHump(key).substring(0, 1).toLowerCase() + lineToHump(key).substring(1);
            newMap.put(key, value);
        }
        return newMap;
    }

    /** ?????????????????? */
    private String lineToHump(String str) {
        str = str.toLowerCase();
        Pattern linePattern = Pattern.compile("_(\\w)");
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
