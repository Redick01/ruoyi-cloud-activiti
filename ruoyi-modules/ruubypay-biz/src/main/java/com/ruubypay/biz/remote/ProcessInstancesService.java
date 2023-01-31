package com.ruubypay.biz.remote;

import org.activiti.rest.service.api.runtime.process.ProcessInstanceCreateRequest;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Redick01
 */
@FeignClient(value = "ruubypay-activiti")
public interface ProcessInstancesService {

    /**
     * 通过业务Key 开始一个流程
     * @param request 请求参数
     * @return 返回参数
     */
    @PostMapping("/runtime/process-instances")
    ProcessInstanceResponse startProcessInstanceByKey(ProcessInstanceCreateRequest request);
}
