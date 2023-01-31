package com.ruubypay.biz;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.swagger.annotation.EnableCustomSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Redick01
 */
@EnableCustomConfig
@EnableCustomSwagger2
@EnableFeignClients(basePackages = "com.ruubypay.biz.remote")
@SpringBootApplication
public class RuubypayBizApplication {

    public static void main( String[] args ) {
        SpringApplication.run(RuubypayBizApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  Biz模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
