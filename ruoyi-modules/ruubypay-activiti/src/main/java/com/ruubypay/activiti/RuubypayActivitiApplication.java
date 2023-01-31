package com.ruubypay.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Redick01
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.activiti.spring.boot.SecurityAutoConfiguration.class
})
@EnableFeignClients(basePackages = "com.ruoyi.system.api")
public class RuubypayActivitiApplication {

    public static void main( String[] args ) {
        SpringApplication.run(RuubypayActivitiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  Activiti模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
