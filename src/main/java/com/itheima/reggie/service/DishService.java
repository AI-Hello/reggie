package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //同时在dish和dishflavor存放数据
    //新增菜品，同时需要插入菜品对应的口味数据，需要操作两张表：dish、dishflavor
    public void saveWithFlavor(DishDto dishDto);
    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
