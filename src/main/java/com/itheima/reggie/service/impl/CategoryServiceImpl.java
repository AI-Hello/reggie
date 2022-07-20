package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @title:CategoryServiceImpl
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 17:43
 * @Version:1.8
 **/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
   //根据id删除分类，删除之前进行判断，看看是否关联套餐或菜品
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联菜品，若关联，则抛出一个业务异常
        //select * from dish where category_id=?
        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(dishLambdaQueryWrapper);
        if(count>0){
            //抛出业务异常
            throw new CustomException("当前分类项关联了菜品，无法删除该信息");
        }
        //查询当前分类是否关联套餐，若关联，则抛出一个业务异常
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if(count1>0){
            //抛出业务异常
            throw new CustomException("当前分类项关联了套餐，无法删除该信息");
        }
        //正常删除
        super.removeById(id);
    }
}
