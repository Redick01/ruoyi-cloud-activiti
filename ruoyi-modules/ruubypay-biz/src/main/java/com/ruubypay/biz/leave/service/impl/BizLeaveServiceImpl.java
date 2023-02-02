package com.ruubypay.biz.leave.service.impl;

import com.ruoyi.activiti.api.RemoteActivitiService;
import com.ruoyi.activiti.api.domain.InstanceBusiness;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.utils.CharConvertUtil;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruubypay.biz.leave.domain.vo.BizLeaveVo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import java.util.Map;
import java.util.Objects;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceCreateRequest;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruubypay.biz.leave.mapper.BizLeaveMapper;
import com.ruubypay.biz.leave.domain.BizLeave;
import com.ruubypay.biz.leave.service.IBizLeaveService;
import org.springframework.util.CollectionUtils;

/**
 * 请假业务Service业务层处理
 * 
 * @author Redick
 * @date 2023-01-29
 */
@Service
public class BizLeaveServiceImpl implements IBizLeaveService 
{
    @Autowired
    private BizLeaveMapper bizLeaveMapper;

    @Autowired
    private RemoteActivitiService remoteActivitiService;

    /**
     * 查询请假业务
     * 
     * @param id 请假业务主键
     * @return 请假业务
     */
    @Override
    public BizLeaveVo selectBizLeaveById(Long id)
    {
        return bizLeaveMapper.selectBizLeaveById(id);
    }

    /**
     * 查询请假业务列表
     * 
     * @param bizLeave 请假业务
     * @return 请假业务
     */
    @Override
    public List<BizLeave> selectBizLeaveList(BizLeave bizLeave) {
        if (!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getSysUser().getUserId())) {
            bizLeave.setCreateBy(SecurityUtils.getUsername());
        }
        List<BizLeave> list = bizLeaveMapper.selectBizLeaveList(bizLeave);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(item -> {
                try {
                    if (StringUtils.isBlank(item.getInstanceId())) {
                        item.setTaskName("未启动");
                    } else {
                        // 根据instanceId获取Task，如果Task存在更新Task信息，如果Task没有查询HistoricTaskInstance
                        Task task = remoteActivitiService.queryTaskListByInstanceId(item.getInstanceId(),
                                SecurityConstants.FROM_SOURCE);
                        if (!Objects.isNull(task)) {
                            TaskEntityImpl taskEntity = (TaskEntityImpl) task;
                            item.setTaskId(taskEntity.getId());
                            if (2 == taskEntity.getSuspensionState()) {
                                item.setTaskName("已挂起");
                                item.setSuspendState("2");
                                item.setSuspendStateName("已挂起");
                            } else {
                                item.setTaskName(taskEntity.getName());
                                item.setSuspendState("1");
                                item.setSuspendStateName("已激活");
                            }
                        } else {
                            // 已办结或者已撤销
                            List<HistoricTaskInstance> taskInstanceList = remoteActivitiService.queryHistoryByInstanceId(item.getInstanceId(),
                                    SecurityConstants.INNER);
                            if (!CollectionUtils.isEmpty(taskInstanceList)) {
                                HistoricTaskInstance taskInstance = taskInstanceList.get(0);
                                if (StringUtils.isNotBlank(taskInstance.getDeleteReason())) {
                                    item.setTaskName("已撤销");
                                } else {
                                    item.setTaskName("已结束");
                                }
                                item.setTaskId("-1");
                            } else {
                                item.setTaskName("流程已删除");
                                item.setTaskId("-2");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return list;
    }

    /**
     * 新增请假业务
     * 
     * @param bizLeave 请假业务
     * @return 结果
     */
    @Override
    public int insertBizLeave( BizLeaveVo bizLeave)
    {
        bizLeave.setCreateBy(SecurityUtils.getUsername());
        bizLeave.setCreateTime(DateUtils.getNowDate());
        return bizLeaveMapper.insertBizLeave(bizLeave);
    }

    /**
     * 修改请假业务
     * 
     * @param bizLeave 请假业务
     * @return 结果
     */
    @Override
    public int updateBizLeave(BizLeave bizLeave)
    {
        bizLeave.setUpdateTime(DateUtils.getNowDate());
        return bizLeaveMapper.updateBizLeave(bizLeave);
    }

    /**
     * 批量删除请假业务
     * 
     * @param ids 需要删除的请假业务主键
     * @return 结果
     */
    @Override
    public int deleteBizLeaveByIds(Long[] ids)
    {
        return bizLeaveMapper.deleteBizLeaveByIds(ids);
    }

    /**
     * 删除请假业务信息
     * 
     * @param id 请假业务主键
     * @return 结果
     */
    @Override
    public int deleteBizLeaveById(Long id)
    {
        return bizLeaveMapper.deleteBizLeaveById(id);
    }

    @Override
    public void submitApply(BizLeaveVo entity, String applyUserId, String key,
            Map<String, Object> variables) {
        entity.setApplyUser(applyUserId);
        entity.setApplyTime(DateUtils.getNowDate());
        entity.setUpdateBy(applyUserId);
        bizLeaveMapper.updateBizLeave(entity);
        // 请假的业务ID，作为流程的key
        String businessKey = entity.getId().toString();
        // 启动流程，设置业务key
        ProcessInstanceCreateRequest request = new ProcessInstanceCreateRequest();
        request.setBusinessKey(businessKey);
        request.setProcessDefinitionKey(key);
        request.setVariables(new ArrayList<>());
        ProcessInstanceResponse response = remoteActivitiService
                .startProcessInstanceByDefinitionId(request);
        // 流程实例ID
        String processInstanceId = response.getId();

        // 调用流程中心启动流程，并启动processInstanceId与业务数据关联起来
        InstanceBusiness instanceBusiness = new InstanceBusiness();
        instanceBusiness.setBusinessKey(businessKey);
        instanceBusiness.setInstanceId(processInstanceId);
        instanceBusiness.setModule(CharConvertUtil.humpToLine(entity.getClass().getSimpleName().substring(1)));
        remoteActivitiService.addInstanceBusiness(instanceBusiness, SecurityConstants.INNER);
        // 更新请假业务流程实例ID
        entity.setInstanceId(processInstanceId);
        bizLeaveMapper.updateBizLeave(entity);
    }
}
