package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @title:LoginCheckFilter
 * @Author:Yuanhaopeng
 * @Data:2022/7/16 16:53
 * @Version:1.8
 **/
//检查用户是否已经完成登录，用过滤器或者拦截器来做
    @WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")  //所有的请求都拦截
    @Slf4j
    //定义拦截器类
public class LoginCheckFilter implements Filter {
        //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    //拦截的都是HttpServletResponse的对象
    @Override
    //因为session的request是HttpServletResponse对象
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        //1.获取本次请求的uri，标识符/employee/login等方式表示
        String requestURI = request.getRequestURI();
        log.info("拦截到的请求{}",requestURI);
        //2.本次请求是否需要处理，若用户访问这些路径直接放行
        String[] urls=new String[]{"/employee/login","/employee/logout","/backend/**","/front/**",
                "/user/sendMsg","/user/login"};
        boolean check = check(requestURI, urls);
        //3.如果不需要处理直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;  //直接结束方法
        }
        //4.不成立的话就需要判断登陆状态，如果成功，则直接放行
        //从session中获取登录信息
        HttpSession session = request.getSession();
        if(session.getAttribute("employee")!=null) {
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("employee"));

            //判断是否是同一个线程，在做公共字段时候用
            //先从LoginCheckFilter中的dofilter方法中设置id，然后从此处获取id值
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        if(session.getAttribute("user")!=null){
                log.info("用户已登录，用户id为{}",request.getSession().getAttribute("user"));

                //判断是否是同一个线程，在做公共字段时候用
                //先从LoginCheckFilter中的dofilter方法中设置id，然后从此处获取id值
                Long userId = (Long)request.getSession().getAttribute("user");
                BaseContext.setCurrentId(userId);

                filterChain.doFilter(request,response);
                return;
        }
        log.info("用户未登录");
        //5.如果未登录则返回未登录结果,通过输出流方式向客户端响应数据
        //因为后面的前端会根据NOTLOGIN来判断是否跳到登陆界面
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }
    //判断request的url与不需拦截的url是否相同
    public boolean check(String requesturi,String[] urls){
        for (String url:urls){
            boolean match = PATH_MATCHER.match(url, requesturi);
            if(match)return true;
        }
        return false;
    }
}
