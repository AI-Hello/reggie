package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @title:DishController
 * @Author:Yuanhaopeng
 * @Data:2022/7/18 10:35
 * @Version:1.8
 **/
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    //新增菜品
    @PostMapping
    //因为服务端传来的json数据包含DishFlavor的类的属性，所以不能用Dish类直接传参
    //通过Dto来封装Dish+DishFlavor类的属性
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        //因为要操作两张表，所以不能简单的DishService.save()
        //同时在dish和dishflavor存放数据，所以要在DishService中另写方法
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }

    //分页展示查询菜品
    //http://localhost:/dish/page?page=1&pageSize=10&name=123
    @GetMapping("/page")
    //因为查询中有一个菜品分类，而不是菜品的id，所以还要根据id查询category中的菜类
    //所以用DTO，而且DishDto有categoryName
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo=new Page(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        //通过对象拷贝

        //构造条件构造器:排序条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        lambdaQueryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Dish::getUpdateTime);
        //执行查询        根据查询条件来返回信息，即page类型的数据
       dishService.page(pageInfo,lambdaQueryWrapper);
       //records就是列表中的数据
       //简单来说下面的功能就是把页面数据抽取出来，然后把名字加上去，再一起装好，然后显示出去
       //对象拷贝
        //除了records其他都拷过去，因为records的数据类型是Dish而不是DishDto
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //现在就要加records的数据，对records的数据进行处理
        List<Dish> records = pageInfo.getRecords();
        //对records进行处理
        List<DishDto> list=records.stream().map((item)->{
            //创建一个新实体，先将其他字段拷过去，然后再将name拷过去
            DishDto dishDto=new DishDto();
            //先对普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,dishDto);
            //拿到分类的id
            Long categoryId = item.getCategoryId();
            //根据id值查询到分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                //获得分类对象的名称
                String categoryName = category.getName();
                //再设置名称
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }


    //回显数据  用到dto，因为修改回显用到了flavor表，所以用dto类型
    //根据id查询菜品信息和口味信息
    //http:localhost/dish/141260074...
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);
    }

    //修改菜品
    //http://dish
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        //dish和dishflavor表都要更新
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功!");
    }

    //更新起售停售功能，批量起售停售
    @PostMapping("/status/{status}")
    //@RequestParam，解决前台参数名称与后台接收参数变量名称不一致的问题，等价于request.getParam
    public R<String> statusWithIds(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        //先构建一个条件构造器
        LambdaQueryWrapper<Dish>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids!=null,Dish::getId,ids);
        lambdaQueryWrapper.orderByDesc(Dish::getPrice);
        //根据条件批量查询
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        for(Dish dish:list){
            if(dish!=null){
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("售卖状态修改成功");

    }

    //删除菜品功能，批量删除
    @DeleteMapping
    @Transactional
    public R<String> deleteWithIds(@RequestParam List<Long> ids){
        //先构造一个条件查询器
        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(ids!=null,Dish::getId,ids);
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);
        for (Dish dish:list){
            //首先删除的是dish表，然后再删除口味表的数据
            //表示停售状态，停售状态可直接删除
            if(dish.getStatus()==0) {
                //直接删除dish
                dishService.removeById(dish.getId());
                //还要删除口味表
                LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper=new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(DishFlavor::getId,dish.getId());
                //删除菜品id关联的口味表信息
                dishFlavorService.remove(lambdaQueryWrapper);
            }
            else throw new CustomException("此菜品还在售卖状态，无法删除！");
        }
        return R.success("删除成功");
    }


//    //根据条件查询对应的菜品数据，实现套餐管理中的增加套餐中的添加菜品
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        //构造查询条件
//        //前端获取所要点击的菜类的id，然后根据这个id来进行查询dish中的菜品
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //只展示在售的商品
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        //// 查询所有数据
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
//        return R.success(list);
//    }



    //根据条件查询对应的菜品数据，实现套餐管理中的增加套餐中的添加菜品
    //在员工后端以及用户的列表中都有显示，代码重用
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        //前端获取所要点击的菜类的id，然后根据这个id来进行查询dish中的菜品
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //只展示在售的商品
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //// 查询所有数据
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        List<DishDto> dishDtos=list.stream().map((item)->{
            //创建一个新实体，先将其他字段拷过去，然后再将name拷过去
            DishDto dishDto=new DishDto();
            //先对普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,dishDto);
            //拿到分类的id
            Long categoryId = item.getCategoryId();
            //根据id值查询到分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                //获得分类对象的名称
                String categoryName = category.getName();
                //再设置名称
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品的id
            //用户的列表中选择规格的口味信息
            Long dishid = item.getId();
            LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper1=new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId,dishid);
            //select * from dish_flavor where dish_id=?
            List<DishFlavor> dishFlavorlist = dishFlavorService.list(lambdaQueryWrapper1);
            dishDto.setFlavors(dishFlavorlist);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }

}
