package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployService;
import org.springframework.stereotype.Service;

/**
 * @title:EmployeeServiceImpl
 * @Author:Yuanhaopeng
 * @Data:2022/7/16 10:51
 * @Version:1.8
 **/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployService {

}
