package com.itheima.reggie.common;

/**
 * @title:BastContext
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 17:17
 * @Version:1.8
 **/
//基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
//每个线程都拥有一个basecontext
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
