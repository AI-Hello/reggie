package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @title:EmployeeController
 * @Author:Yuanhaopeng
 * @Data:2022/7/16 10:54
 * @Version:1.8
 **/
//员工登录
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployService employService;
    @PostMapping("/login")  //httpservletrequest获取请求行信息的方法
    //@RequestBody接受来自客户端的Josin数据（请求体的数据）送到employee中
    //员工登录
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        //DigestUtils可以实现md5的加密：用户密码通过md5加密才行
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库,用lambda来做
        LambdaQueryWrapper<Employee> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Employee> eq = objectLambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        log.info("eq={}",eq);
        Employee emp = employService.getOne(eq);

        //3.如果没有查询到则返回登陆失败结果
        if(emp==null){
            return R.error("没有该账户，请注册！");
        }
        //4.比对密码，如果不一致则返回登陆失败结果
        if(!emp.getPassword().equals(password))
        {
            return R.error("密码错误，登录失败!");
        }
        //5.查看员工状态
        if(emp.getStatus()==0){
            return R.error("账号已经禁用");
        }
        //6.登陆成功，将用户id存入到session并返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    //员工退出
    @PostMapping("/logout")  //退出是post请求，所以用post方式注解
    public R<String> logout(HttpServletRequest request){
        //1.清除session中的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

    //新增员工
    @PostMapping  //因为请求到employee之后就没了，所以不需要加
    //@RequestBody是加载对象上的
    public R<String> save(HttpServletRequest request,@RequestBody  Employee employee){
        //因为刚创建员工的时候没有密码，所以需要初始化一个员工密码
        //初始密码为123456，但是要md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //更新时间----让元数据处理器来自动填充
//        employee.setCreateTime(LocalDateTime.now()); //创建这条记录的时间
//        employee.setUpdateTime(LocalDateTime.now()); //更新时间
        //获得当前用户的id(Long型的)
//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);  //创建用户的人
//        employee.setUpdateUser(empId);  //更新用户的人
        //保存数据到数据库中，而save方法是Iservice接口自动提供好的方法直接调用即可
        employService.save(employee);
        return R.success("新增员工成功！");
    }

    //分页查询的方法
    @GetMapping("/page")
    public R<Page> page( int page, int pageSize, String name){
        log.info("page={},pagesize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        lambdaQueryWrapper.like(Strings.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询        根据查询条件来返回信息，即page类型的数据
        employService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    //根据员工id修改员工信息，将禁用和修改员工信息公用该代码
    @PutMapping//前端的修改和禁用启用都用到了put，所以都返回这个函数
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("employee={}",employee.toString());
        //更新时间----让元数据处理器来自动填充
//        employee.setUpdateTime(LocalDateTime.now());
//       Long emp1Id = (Long) request.getSession().getAttribute("employee");
        //更新是谁操作的
//        employee.setUpdateUser(emp1Id);
        //js对long型的数据丢失精度，所以id显示的会不同

        //判断是否是同一个线程，在做公共字段时候用
        long id = Thread.currentThread().getId();
        log.info("线程id为{}",id);

        employService.updateById(employee);
        return R.success("员工信息修改成功！");
    }

    //根据id查询员工信息，并根据修改的id值反馈给update方法
    //lockhost://employee/id
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employService.getById(id);
        if(employee!=null) return R.success(employee);
        //前端的修改和禁用启用都用到了put，只要页面回显然后修改数据后还会返回update方法
        else return R.error("无员工信息！");
    }
}
