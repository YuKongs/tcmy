package com.yukong.controller;

import com.yukong.common.R;
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
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传与下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${zxsm.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        // file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());
        // 获得原始文件名
        String originalFilename = file.getOriginalFilename(); // abc.jpg

        // substring(index); 从下标开始处进行分割，包括当前下标
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        log.info("当前文件名后缀：{}",suffix);

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix; // dafafdsfa + .jpg

        // 创建一个目录对象
        File dir = new File(basePath);
        // 判断当前目录是否存在
        if(!dir.exists()){
            // 目录不存在，需要创建
            dir.mkdirs();
        }
        try {
            // 将临时文件转存到指定位置 （我的电脑就相当于一个服务器）
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")

    public void download(String name, HttpServletResponse response){
        try {
            // 输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] buffer = new byte[1024];
            while((len = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0, len);
                outputStream.flush();
            }

            // 关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
