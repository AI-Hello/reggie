package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @title:SetmealController
 * @Author:Yuanhaopeng
 * @Data:2022/7/19 10:13
 * @Version:1.8
 **/
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    //http://localhost/setmeal
    @PostMapping
    //因为提交过来的数据不仅有setmeal表中的数据还有setmeal_dish表中的数据，所以需要SetmealDao
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        //新增套餐不仅与套餐(setmeal)关系表新增数据还要在套餐和菜品表(setmeal_dish)数据有关系
        //所以需要在创建一张表setmealdto，然后重写方法setmeal的save操作
        setmealService.saveWithDish(setmealDto);
        return R.success("套餐添加成功");
    }


    @GetMapping("/page")
    //因为查询中有一个菜品分类，而不是菜品的id，所以还要根据id查询category中的菜类
    //所以用DTO，而且SetmealDto有categoryName
    public R<Page> page(int page, int pageSize, String name){
        //构造分页构造器
        Page<Setmeal> pageInfo=new Page(page,pageSize);
        Page<SetmealDto> dishDtoPage=new Page<>();
        //通过对象拷贝

        //构造条件构造器:排序条件
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        lambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Setmeal::getUpdateTime);
        //执行查询        根据查询条件来返回信息，即page类型的数据
        setmealService.page(pageInfo,lambdaQueryWrapper);
        //records就是列表中的数据
        //简单来说下面的功能就是把页面数据抽取出来，然后把名字加上去，再一起装好，然后显示出去
        //对象拷贝
        //除了records其他都拷过去，因为records的数据类型是setmeal而不是setmealDto
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //现在就要加records的数据，对records的数据进行处理
        List<Setmeal> records = pageInfo.getRecords();
        //对records进行处理  List<Setmeal>- -- > List<SetmealDto>
        List<SetmealDto> list=records.stream().map((item)->{
            //创建一个新实体，先将其他字段拷过去，然后再将name拷过去
            SetmealDto setmealDto=new SetmealDto();
            //先对普通属性拷贝到dishDto中
            BeanUtils.copyProperties(item,setmealDto);
            //拿到分类的id
            Long categoryId = item.getCategoryId();
            //根据id值查询到分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                //获得分类对象的名称
                String categoryName = category.getName();
                //再设置名称
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    //更新起售停售功能，批量起售停售
    @PostMapping("/status/{status}")
    //@RequestParam，解决前台参数名称与后台接收参数变量名称不一致的问题，等价于request.getParam
    public R<String> statusWithIds(@PathVariable("status") Integer status,@RequestParam List<Long> ids){
        //先构建一个条件构造器
        LambdaQueryWrapper<Setmeal>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids!=null,Setmeal::getId,ids);
        lambdaQueryWrapper.orderByDesc(Setmeal::getPrice);
        //根据条件批量查询
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        for(Setmeal dish:list){
            if(dish!=null){
                dish.setStatus(status);
                setmealService.updateById(dish);
            }
        }
        return R.success("售卖状态修改成功");

    }
    //删除套餐功能，批量删除
    @DeleteMapping
    @Transactional
    public R<String> deleteWithIds(@RequestParam List<Long> ids){
        //先构造一个条件查询器
        LambdaQueryWrapper<Setmeal>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(ids!=null,Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(dishLambdaQueryWrapper);
        for (Setmeal dish:list){
            //首先删除的是dish表，然后再删除口味表的数据
            //表示停售状态，停售状态可直接删除
            if(dish.getStatus()==0) {
                //直接删除dish
                setmealService.removeById(dish.getId());
                //还要删除口味表
                LambdaQueryWrapper<SetmealDish>lambdaQueryWrapper=new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SetmealDish::getId,dish.getId());
                //删除菜品id关联的口味表信息
                setmealDishService.remove(lambdaQueryWrapper);
            }
            else throw new CustomException("此套裁还在售卖状态，无法删除！");
        }
        return R.success("删除成功");
    }

    //用于界面的套餐信息list
    //http:localhost/setmeal/list?categoryId=1413...&status=1
    //setmeal中有categoryId和status信息，所以直接接受实体即可
    @GetMapping("/list")
    public R<List<Setmeal>> list( Setmeal setmeal){
        LambdaQueryWrapper<Setmeal>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return R.success(list);

    }
}
