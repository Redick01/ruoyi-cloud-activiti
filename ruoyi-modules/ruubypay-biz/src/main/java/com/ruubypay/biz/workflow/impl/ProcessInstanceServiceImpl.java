package com.ruubypay.biz.workflow.impl;

import com.ruubypay.biz.remote.ProcessInstancesService;
import com.ruubypay.biz.workflow.IProcessInstanceService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Redick01
 */
@Service
public class ProcessInstanceServiceImpl implements IProcessInstanceService {

    @Autowired
    private ProcessInstancesService processInstancesService;

    @Override
    public String processInstanceId( String applyUserId, String businessKey, String module,
            Map<String, Object> variables ) {
        // 设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        return null;
    }
}
