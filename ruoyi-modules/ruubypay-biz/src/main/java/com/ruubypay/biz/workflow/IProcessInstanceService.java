package com.ruubypay.biz.workflow;

import java.util.Map;

/**
 * @author Redick01
 */
public interface IProcessInstanceService {

    String processInstanceId(String applyUserId, String businessKey, String module, Map<String, Object> variables);
}
