package com.ruubypay.biz.leave.mapper;

import com.ruubypay.biz.leave.domain.vo.BizLeaveVo;
import java.util.List;
import com.ruubypay.biz.leave.domain.BizLeave;

/**
 * 请假业务Mapper接口
 * 
 * @author Redick
 * @date 2023-01-29
 */
public interface BizLeaveMapper 
{
    /**
     * 查询请假业务
     * 
     * @param id 请假业务主键
     * @return 请假业务
     */
    public BizLeaveVo selectBizLeaveById(Long id);

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
     * 删除请假业务
     * 
     * @param id 请假业务主键
     * @return 结果
     */
    public int deleteBizLeaveById(Long id);

    /**
     * 批量删除请假业务
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBizLeaveByIds(Long[] ids);
}
