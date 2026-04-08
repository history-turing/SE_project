package com.whu.treehole.server;

/* 启动模块负责扫描整个多模块工程并暴露 REST 服务。 */

import com.whu.treehole.server.config.AuthProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.whu.treehole")
@MapperScan("com.whu.treehole.infra.mapper")
@EnableConfigurationProperties(AuthProperties.class)
public class TreeholeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TreeholeBackendApplication.class, args);
    }
}
