package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @title:CategoryController
 * @Author:Yuanhaopeng
 * @Data:2022/7/17 17:44
 * @Version:1.8
 **/
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //路径写不写要看页面请求的url是否需要
    @PostMapping
    public R<String> save(@RequestBody Category category){
        //菜类别是唯一的，不能重复，若出现重复的菜类，会进入全局异常处理器来处理
        categoryService.save(category);
        return R.success("新增分类成功!");
    }

    //菜品分页查询的方法
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pagesize={}",page,pageSize);
        //构造分页构造器
        Page<Category> pageInfo=new Page(page,pageSize);
        //构造条件构造器:排序条件
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
//        lambdaQueryWrapper.like(Strings.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        //执行查询        根据查询条件来返回信息，即page类型的数据
        categoryService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //删除菜类
    ////localhost://category?id=12323243543l所以不需要额外加路径了

    @DeleteMapping
    //返回code值，code就是String类型的
    //通过url传输的id值，所以不需要@RequestBody Employee employee
    //localhost://category?id=12323243543l
    public R<String> delete(Long id){
        categoryService.remove(id);
//        categoryService.removeById(id);
        return R.success("删除成功！");
    }

    //修改分类
    @PutMapping
    //传过来的是jason数据
    //当前端传来的值，不是个完整的对象，只是包含了 Req 中的部分参数时，不需要@RequestBody
    //而如果传来的是整个类对象，则需要用@RequestBody
    public R<String> update(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("修改成功！");
    }

    //获取菜品分类列表，根据条件查询分类数据
    //因为路径是/category/list所以将菜品分类卸载category中
    @GetMapping("/list")
    //数据放到请求体里面采用@RequestBody
    //当前端传来的值，不是个完整的对象，只是包含了 Req 中的部分参数时，不需要@RequestBody
    public R<List<Category>> list( Category category){
        //条件构造器
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加条件
        categoryLambdaQueryWrapper.eq(category.getType()!=null,Category::getType, category.getType());
        //添加排序条件 //先按sort排序，sort相同的再按照更新时间排序
        categoryLambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //展示出现执行条件后的数据
        List<Category> list = categoryService.list(categoryLambdaQueryWrapper);
        //返回jason数据
        return R.success(list);
    }
}
