package com.ruubypay.activiti.service;

import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruubypay.activiti.domain.HistoricActivity;
import com.ruubypay.activiti.domain.TaskVo;
import java.util.List;

/**
 * @author Redick01
 */
public interface IProcessService {

    /**
     * 查询审批历史列表
     * @param historicActivity {@link HistoricActivity}
     * @return 历史列表
     */
    List<HistoricActivity> selectHistoryList(HistoricActivity historicActivity);

    /**
     * 转办任务
     * @param taskId 任务ID
     * @param fromUser 源用户
     * @param delegateToUser 转办用户
     */
    void delegate(String taskId, String fromUser, String delegateToUser);

    /**
     * 撤销流程
     * @param instanceId 实例id
     * @param deleteReason 撤销原因
     */
    void cancelApply(String instanceId, String deleteReason);

    /**
     * 激活/挂起流程实例
     * @param instanceId 实例ID
     * @param suspendState 挂起状态
     */
    void suspendOrActiveApply(String instanceId, String suspendState);

    /**
     * 我得待办
     * @param taskVo 任务
     * @return 待办数据
     */
    TableDataInfo findTodoTasks(TaskVo taskVo);

    /**
     * 我的已办
     * @param taskVo 任务
     * @return 已办数据
     */
    TableDataInfo findDoneTasks(TaskVo taskVo);

    /**
     * 办理任务
     * @param taskId 任务ID
     * @param instanceId 实例ID
     * @param variables 动态参数
     */
    void complete(String taskId, String instanceId, String variables);

    /**
     * 填充流程相关字段
     * @param entity 业务实体
     * @param <T> 返回实体
     * @throws Exception {@link Exception}
     */
    <T> void richProcessField(T entity);
}
