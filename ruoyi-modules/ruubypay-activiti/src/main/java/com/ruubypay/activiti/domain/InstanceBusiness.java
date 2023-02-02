package com.ruubypay.activiti.domain;

import com.ruoyi.common.core.utils.StringUtils;
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

    public boolean isNotEmpty() {
        return null != this.id && StringUtils.isNotEmpty(this.instanceId)
                && StringUtils.isNotEmpty(this.businessKey) && StringUtils.isNotEmpty(this.module);
    }
}
