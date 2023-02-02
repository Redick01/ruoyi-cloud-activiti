package com.ruubypay.activiti.controller;

import com.ruoyi.common.core.domain.R;
import java.util.List;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/queryTaskListByInstanceId/{instanceId}")
    public Task queryTaskListByInstanceId(@PathVariable("instanceId") String instanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .list().get(0);
    }
}
