package com.ruoyi.system.api;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.factory.RemoteRoleFallbackFactory;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Redick01
 */
@FeignClient(contextId = "remoteRoleService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteRoleFallbackFactory.class)
public interface RemoteRoleService {

    /**
     * 角色列表
     * @param sysRole 角色数据
     * @param source {@link String}
     * @return 角色列表
     */
    @PostMapping("/role/selectRoleList")
    public R<List<SysRole>> selectRoleList(SysRole sysRole, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
