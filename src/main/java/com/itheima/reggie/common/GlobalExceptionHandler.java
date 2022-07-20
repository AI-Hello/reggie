package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @title:GlobalExceptionHandler
 * @Author:Yuanhaopeng
 * @Data:2022/7/16 20:49
 * @Version:1.8
 **/
//全局异常处理
    //对注解中含有RestController， Controller的类进行异常处理
    @ControllerAdvice(annotations = {RestController.class, Controller.class})
    @ResponseBody  //封装jasion数据来返回
    @Slf4j
public class GlobalExceptionHandler {
        //处理SQLIntegrityConstraintViolationException的异常
        @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
        public  R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
            log.error(ex.getMessage());
            //若出现这种异常，进行分割获得账号参数
            if(ex.getMessage().contains("Duplicate entry"))
            {
                String[] s = ex.getMessage().split(" ");
                String msg=s[2]+"已经存在，不能重复创建";
                return R.error(msg);
            }
            return R.error("未知错误！");
        }

    //处理菜品删除的异常
    @ExceptionHandler(CustomException.class)
    public  R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
