package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @title:MyMetaObjecthandler
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 16:30
 * @Version:1.8
 **/
//自定义元数据对象处理器
    @Component
    @Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
   //在插入时自动填充数据
   @Override
    public void insertFill(MetaObject metaObject) {
       log.info("meatobject-insert",metaObject.toString());
       //对应的是employee的属性名
       //判断是否是同一个线程，在做公共字段时候用，通过线程来获取id
       //先从LoginCheckFilter中的dofilter方法中设置id，然后从此处获取id值
       metaObject.setValue("createTime", LocalDateTime.now());
       metaObject.setValue("createUser",BaseContext.getCurrentId());
       metaObject.setValue("updateTime",LocalDateTime.now());
       metaObject.setValue("updateUser",BaseContext.getCurrentId());
   }

    //更新时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("meatobject-update",metaObject.toString());

        //判断是否是同一个线程，在做公共字段时候用

        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
