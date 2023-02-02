package com.ruubypay.activiti.controller;

import com.ruoyi.common.core.domain.R;
import com.ruubypay.activiti.domain.InstanceBusiness;
import com.ruubypay.activiti.mapper.TaskMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@RestController
@RequestMapping("/instance-business")
public class InstanceBusinessController {

    @Autowired
    private TaskMapper taskMapper;

    @PostMapping("/addInstanceBusiness")
    @ResponseBody
    public R<?> addInstanceBusiness(@RequestBody InstanceBusiness instanceBusiness) {
        if (!instanceBusiness.isNotEmpty()) {
            return R.fail("必填参数为空");
        }
        // 记录流程实例业务关系
        InstanceBusiness ib = new InstanceBusiness();
        BeanUtils.copyProperties(instanceBusiness, ib);
        taskMapper.insertInstanceBusiness(ib);
        return R.ok();
    }
}
