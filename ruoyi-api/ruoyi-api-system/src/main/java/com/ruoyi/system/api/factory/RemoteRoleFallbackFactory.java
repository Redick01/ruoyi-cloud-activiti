package com.ruoyi.system.api.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRoleService;
import com.ruoyi.system.api.domain.SysRole;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author Redick01
 */
@Component
public class RemoteRoleFallbackFactory implements FallbackFactory<RemoteRoleService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteRoleFallbackFactory.class);

    @Override
    public RemoteRoleService create(Throwable cause) {
        log.error("角色服务调用失败:{}", cause.getMessage());
        return new RemoteRoleService() {
            @Override
            public R<List<SysRole>> selectRoleList(SysRole sysRole, String source) {
                return R.fail("获取角色列表失败:" + cause.getMessage());
            }
        };
    }
}
