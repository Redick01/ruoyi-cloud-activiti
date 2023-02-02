package com.ruoyi.activiti.api.domain;

import lombok.Data;

/**
 * 流程实例业务关系表
 * @author Redick
 */
@Data
public class InstanceBusiness {

    private Long id;

    private String instanceId;

    private String businessKey;

    private String module;

}
