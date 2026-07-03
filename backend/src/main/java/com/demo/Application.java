package com.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小区物业管理系统 后端启动类
 * 运行 main 方法即启动，默认端口 8080
 */
@SpringBootApplication
@MapperScan("com.demo.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
