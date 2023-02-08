package com.ruoyi.activiti.api;

import com.ruoyi.activiti.api.domain.InstanceBusiness;
import com.ruoyi.activiti.api.domain.ProcessInstanceStartRequest;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import java.util.List;
import java.util.Map;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceCreateRequest;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Redick01
 */
@FeignClient(contextId = "remoteActivitiService", value = ServiceNameConstants.ACTIVITI_SERVICE)
public interface RemoteActivitiService {

    /**
     * 记录流程实例业务关系
     * @param instanceBusiness 流程实例和业务关联
     * @param source {@link RequestHeader}
     * @return R
     */
    @PostMapping("/instance-business/addInstanceBusiness")
    R<?> addInstanceBusiness(@RequestBody InstanceBusiness instanceBusiness,
            @RequestHeader(SecurityConstants.INNER) String source);

    /**
     * 启动流程
     * @param request 请求参数
     * @return ProcessInstanceResponse
     */
    @PostMapping("/runtime/process-instances")
    ProcessInstanceResponse startProcessInstanceByDefinitionId(@RequestBody
            ProcessInstanceCreateRequest request);

    /**
     * 开始一个流程
     * @param request 请求参数
     * @return 流程实例
     */
    @PostMapping("/process/startProcessInstance")
    @ResponseBody R<ProcessInstanceResponse> startProcessInstance(@RequestBody ProcessInstanceStartRequest request);

    /**
     * 根据流程实例ID获取Task列表
     * @param instanceId 实例ID
     * @param source source
     * @return Task列表
     */
    @GetMapping("/task/queryTaskListByInstanceId/{instanceId}")
    R<Map<String, Object>> queryTaskListByInstanceId(@PathVariable("instanceId") String instanceId,
            @RequestHeader(SecurityConstants.INNER) String source);

    /**
     * 根据instanceId查询历史列表
     * @param instanceId instanceId
     * @param source source
     * @return 历史列表
     */
    @GetMapping("/history/queryHistoryByInstanceId/{instanceId}")
    R<List<HistoricTaskInstance>> queryHistoryByInstanceId(@PathVariable("instanceId") String instanceId,
            @RequestHeader(SecurityConstants.INNER) String source);
}
