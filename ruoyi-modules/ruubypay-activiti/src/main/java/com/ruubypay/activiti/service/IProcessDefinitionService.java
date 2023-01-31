package com.ruubypay.activiti.service;

import com.github.pagehelper.Page;
import com.ruubypay.activiti.domain.ProcessDefinition;

/**
 * @author Redick01
 */
public interface IProcessDefinitionService {

    Page<ProcessDefinition> listProcessDefinition(ProcessDefinition processDefinition);

    void deployProcessDefinition(String filePath);

    int deleteProcessDeploymentByIds(String deploymentIds) throws Exception;

    void suspendOrActiveDefinition(String id, String suspendState);

}
