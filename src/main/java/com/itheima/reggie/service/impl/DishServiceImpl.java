package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.sun.deploy.ui.DialogTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @title:DishServiceImpl
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 20:07
 * @Version:1.8
 **/
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，同时保存对应的口味数据
    @Override
    @Transactional //用事务保证数据的一致性
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        //因为dishDto继承dish所以可以直接保存
        this.save(dishDto);

        //保存菜品口味数据到口味表dish_flavor
        //这样无法保存dishid，所以要进行处理
        Long id = dishDto.getId();  //对应的菜品id
        //遍历一个列表，对列表中的属性进行转换、赋值等操作形成我们想要的一个新列表。
        //通常我们的常规思路就是直接使用for循环。
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        //因为要返回一个DishDto对象，所以要先将菜品的基本信息保存到dishdto中再保存flavors
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //再查询当前菜品对应的口味信息，从dish_flavor中查询
        //根据菜品id值来查询口味的id值，然后把相同的id值的口味信息全部展示出来
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表的基本信息
        this.updateById(dishDto);
        //更新dish_flavor表
        //1.先删除菜品对应口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor>queryWrapper=new LambdaQueryWrapper<>();
        //找到dish中id与dish_flavor中对应dish的id相同的数据
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //2.添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }
}
