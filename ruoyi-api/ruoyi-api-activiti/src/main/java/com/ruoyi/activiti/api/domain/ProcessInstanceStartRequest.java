package com.ruoyi.activiti.api.domain;

import java.util.Map;
import lombok.Data;

/**
 * @author Redick01
 */
@Data
public class ProcessInstanceStartRequest {

    private String username;

    private String processDefinitionKey;

    private String businessKey;

}
