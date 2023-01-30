package com.ruubypay.biz.leave.service;

import java.util.List;
import com.ruubypay.biz.leave.domain.BizLeave;

/**
 * 请假业务Service接口
 * 
 * @author Redick
 * @date 2023-01-29
 */
public interface IBizLeaveService 
{
    /**
     * 查询请假业务
     * 
     * @param id 请假业务主键
     * @return 请假业务
     */
    public BizLeave selectBizLeaveById(Long id);

    /**
     * 查询请假业务列表
     * 
     * @param bizLeave 请假业务
     * @return 请假业务集合
     */
    public List<BizLeave> selectBizLeaveList(BizLeave bizLeave);

    /**
     * 新增请假业务
     * 
     * @param bizLeave 请假业务
     * @return 结果
     */
    public int insertBizLeave(BizLeave bizLeave);

    /**
     * 修改请假业务
     * 
     * @param bizLeave 请假业务
     * @return 结果
     */
    public int updateBizLeave(BizLeave bizLeave);

    /**
     * 批量删除请假业务
     * 
     * @param ids 需要删除的请假业务主键集合
     * @return 结果
     */
    public int deleteBizLeaveByIds(Long[] ids);

    /**
     * 删除请假业务信息
     * 
     * @param id 请假业务主键
     * @return 结果
     */
    public int deleteBizLeaveById(Long id);
}
