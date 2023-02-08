package com.ruubypay.activiti.controller;

import com.ruoyi.common.core.domain.R;
import com.ruubypay.activiti.util.CommUtil;
import java.util.List;
import java.util.Map;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.Task;
import org.apache.poi.ss.formula.functions.T;
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
    public R<Map<String, Object>> queryTaskListByInstanceId(@PathVariable("instanceId") String instanceId) {
        List<Task> list = taskService.createTaskQuery()
                .processInstanceId(instanceId)
                .list();
        if (list.size() > 0) {
            Task task = list.get(0);
            TaskEntityImpl taskEntity = (TaskEntityImpl) task;
            return R.ok(CommUtil.obj2map(taskEntity, CommUtil.TASK_PS));
        } else {
            return R.ok();
        }
    }
}
