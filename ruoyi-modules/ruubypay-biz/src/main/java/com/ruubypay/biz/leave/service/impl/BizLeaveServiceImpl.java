package com.ruubypay.biz.leave.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
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
    public BizLeave selectBizLeaveById(Long id)
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
    public int insertBizLeave(BizLeave bizLeave)
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
}
