package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.config.SMSUtils;
import com.itheima.reggie.config.ValidateCodeUtils;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @title:UserController
 * @Author:Yuanhaopeng
 * @Data:2022/7/19 14:42
 * @Version:1.8
 **/
@RequestMapping("/user")
@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    //获取手机号和验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(Strings.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //随机生成4位验证码
//            SMSUtils.sendMessage("瑞吉外卖", "", phone, code);
            //调用阿里云短信服务API完成短信服务
            //验证码存到session中
            session.setAttribute(phone,code);
            return R.success("手机发送成功");
        }
        return R.success("手机发送失败");
    }

    //用户登录
    @PostMapping("/login")
    //Session中的Json格式为phone：，code：
    //user中没有code所以不能用user对象传参
    //Map与json一一对应，用键值对的形式
    public R<User> login(HttpSession session, @RequestBody Map map){
        //1.获取手机号
        String phone = map.get("phone").toString();
        //2.获取验证码
        String code = map.get("code").toString();
        //3.从Session中获取保存的验证码，因为key是phone，code是value
        Object codeInSession = session.getAttribute(phone);
        //4.进行验证码比对，页面提交验证码与Session中的验证码进行比对
        if(codeInSession.equals(code)) {
            //5.若比对成功，说明登陆成功
            //6.判断当前手机号是否为新用户，若是自动完成注册存到数据库
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            //若是自动完成注册存到数据库
            if(user==null){
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
