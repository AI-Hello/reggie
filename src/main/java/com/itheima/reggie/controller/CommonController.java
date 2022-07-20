package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @title:CommonController
 * @Author:Yuanhaopeng
 * @Data:2022/7/18 8:37
 * @Version:1.8
 **/
//文件上传和下载
   @RestController
   //http://localhost:8080/common/upload
   @RequestMapping("/common")
   @Slf4j
public class CommonController {
       //MultipartFile跟网页的file名保持一致,file是临时文件
       //动态生成路径,value注解可动态获取外部值注入到bean中，常用字符串，文件资源以及url
        //value的参数一般在application.yml中指定
       @Value("${reggie.path}")
       private String basePath;

       @PostMapping("/upload")
       public R<String> upload(MultipartFile file){
           log.info(file.toString());
           //获取原始文件名（上传时候的文件名）
           String originalFilename = file.getOriginalFilename();
           //获取后缀名
           String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
           //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
           String filename= UUID.randomUUID().toString()+substring;

           //创建一个目录对象，若目录不存在就创建一个目录
           File dir=new File(basePath);
           if(!dir.exists()){
               //目录不存在，则创建目录
               dir.mkdirs();
           }
           try {
               //将临时文件转存到指定位置
               file.transferTo(new File(basePath+filename));
           } catch (IOException e) {
               e.printStackTrace();
           }
           //文件传完之后需要将文件名传到数据库中，以便后续操作
           return R.success(filename);
       }

       //文件下载
    @GetMapping("/download")
    //前端的文件传回来的是name，所以根据name来整
    public void download(String name, HttpServletResponse response){
        try {
            //输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //输出流通过输出流将文件写回游览器，在游览器展示图片了
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];
            //开始读文件，然后通过输出流写回游览器
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
