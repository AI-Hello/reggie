package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @title:SetmealServiceImpl
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 20:08
 * @Version:1.8
 **/
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    //新增套餐同时保存套餐和菜品的关联关系
    @Transactional  //两个表的操作，保证表的一致性
    public void saveWithDish(@RequestBody SetmealDto setmealDto) {
        //因为setmealDto继承setmeal所以可以直接保存
        this.save(setmealDto);
        //保存套餐和菜品的关联信息，操作set——meal表
        Long id = setmealDto.getId();  //对应的菜品id

        //遍历一个列表，对列表中的属性进行转换、赋值等操作形成我们想要的一个新列表。
        //通常我们的常规思路就是直接使用for循环。
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
