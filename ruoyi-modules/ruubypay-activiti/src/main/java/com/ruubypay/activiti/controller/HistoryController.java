package com.ruubypay.activiti.controller;

import com.ruoyi.common.core.domain.R;
import java.util.List;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    // Todo 待使用Activiti接口测试
    @GetMapping("/queryHistoryByInstanceId/{instanceId}")
    public List<HistoricTaskInstance> queryHistoryByInstanceId(@PathVariable("instanceId") String instanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }
}
