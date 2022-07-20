package com.itheima.reggie.common;

/**
 * @title:CustomException
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 20:36
 * @Version:1.8
 **/
//自定义业务异常
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
