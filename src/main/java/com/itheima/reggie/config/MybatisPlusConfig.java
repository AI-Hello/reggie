package com.itheima.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @title:MybatisPlusConfig
 * @Author:Yuanhaopeng
 * @Data:2022/7/16 21:38
 * @Version:1.8
 **/
//配置MP的分页插件
@Configuration
public class MybatisPlusConfig {
    //配置拦截器
    @Bean   //给容器中添加组件，以方法名作为组件id，返回类型就是组件类型，返回值就是组件在容器中的实例
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //定义拦截器的壳子
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //建立一个拦截器----分页拦截器
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
