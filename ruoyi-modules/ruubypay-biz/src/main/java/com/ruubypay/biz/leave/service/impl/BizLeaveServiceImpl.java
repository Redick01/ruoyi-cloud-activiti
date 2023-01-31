package com.ruubypay.biz.leave.service.impl;

import com.ruubypay.biz.leave.domain.vo.BizLeaveVo;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruubypay.biz.leave.mapper.BizLeaveMapper;
import com.ruubypay.biz.leave.domain.BizLeave;
import com.ruubypay.biz.leave.service.IBizLeaveService;

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
    public List<BizLeave> selectBizLeaveList(BizLeave bizLeave)
    {
        return bizLeaveMapper.selectBizLeaveList(bizLeave);
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
    public void submitApply( BizLeaveVo entity, String applyUserId, String key,
            Map<String, Object> variables ) {
        entity.setApplyUser(applyUserId);
        entity.setApplyTime(DateUtils.getNowDate());
        entity.setUpdateBy(applyUserId);
        bizLeaveMapper.updateBizLeave(entity);
        // 请假的业务ID，作为流程的key
        String businessKey = entity.getId().toString();
        // 调用流程中心启动流程，并启动processInstanceId与业务数据关联起来

        // 流程实例ID
        String processInstanceId = "";
        entity.setInstanceId(processInstanceId);
        bizLeaveMapper.updateBizLeave(entity);
    }
}
