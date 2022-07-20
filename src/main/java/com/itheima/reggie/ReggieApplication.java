package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @title:ReggieApplication
 * @Author:Yuanhaopeng
 * @Data:2022/7/15 21:12
 * @Version:1.8
 **/
@Slf4j   //lombok加入的，编写log日志
@SpringBootApplication
@ServletComponentScan   //扫描过滤器的注解
@EnableTransactionManagement
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功");
    }
}
