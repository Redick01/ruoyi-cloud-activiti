package com.ruubypay.activiti.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl;

/**
 * @author Redick01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HistoricActivity extends HistoricActivityInstanceEntityImpl {

    /** 审批批注 */
    private String comment;

    /** 办理人姓名 */
    private String assigneeName;
}
