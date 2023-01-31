package com.ruubypay.biz.remote;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Redick01
 */
@FeignClient(value = "ruubypay-activiti")
public interface IdentityService {

}
