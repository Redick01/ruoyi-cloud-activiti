package com.ruubypay.biz.leave.controller;

import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.LoginUser;
import com.ruubypay.biz.leave.domain.vo.BizLeaveVo;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruubypay.biz.leave.domain.BizLeave;
import com.ruubypay.biz.leave.service.IBizLeaveService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 请假业务Controller
 * 
 * @author Redick
 * @date 2023-01-29
 */
@RestController
@RequestMapping("/leave")
public class BizLeaveController extends BaseController
{
    @Autowired
    private IBizLeaveService bizLeaveService;

    /**
     * 查询请假业务列表
     */
    @RequiresPermissions("leave:leave:list")
    @GetMapping("/list")
    public TableDataInfo list(BizLeave bizLeave)
    {
        startPage();
        List<BizLeave> list = bizLeaveService.selectBizLeaveList(bizLeave);
        return getDataTable(list);
    }

    /**
     * 导出请假业务列表
     */
    @RequiresPermissions("leave:leave:export")
    @Log(title = "请假业务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizLeave bizLeave)
    {
        List<BizLeave> list = bizLeaveService.selectBizLeaveList(bizLeave);
        ExcelUtil<BizLeave> util = new ExcelUtil<BizLeave>(BizLeave.class);
        util.exportExcel(response, list, "请假业务数据");
    }

    /**
     * 获取请假业务详细信息
     */
    @RequiresPermissions("leave:leave:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bizLeaveService.selectBizLeaveById(id));
    }

    /**
     * 新增请假业务
     */
    @RequiresPermissions("leave:leave:add")
    @Log(title = "请假业务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLeaveVo bizLeave)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser.getRoles().contains("admin")) {
            return error("提交申请失败：不允许管理员提交申请！");
        }
        return toAjax(bizLeaveService.insertBizLeave(bizLeave));
    }

    @Log(title = "提交请假单", businessType = BusinessType.UPDATE)
    @PostMapping("/submitApply")
    @ResponseBody
    public AjaxResult submitApply(Long id) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(id);
        String applyUserId = SecurityUtils.getUsername();

        return success();
    }

    /**
     * 修改请假业务
     */
    @RequiresPermissions("leave:leave:edit")
    @Log(title = "请假业务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLeave bizLeave)
    {
        return toAjax(bizLeaveService.updateBizLeave(bizLeave));
    }

    /**
     * 删除请假业务
     */
    @RequiresPermissions("leave:leave:remove")
    @Log(title = "请假业务", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(bizLeaveService.deleteBizLeaveByIds(ids));
    }
}
