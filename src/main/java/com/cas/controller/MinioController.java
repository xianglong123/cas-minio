package com.cas.controller;

import com.cas.util.MinIOUtils;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/minio")
@Api(tags = "minio测试")
@RequiredArgsConstructor
@Slf4j
public class MinioController {

    private static final String TEST = "test";

    private static LinkedList<String> list = new LinkedList<>();

    @Resource
    private MinioClient minioClient;

    @PostMapping("/upload")
    public void upload(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        String filename = file.getOriginalFilename();
        list.push(filename);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(TEST)
                        .object(filename)
                        .contentType("Image/png")
                        .stream(inputStream, inputStream.available(), -1)
                        .build());
    }

    /**
     * 下载最近一次上传的对象
     *
     * @throws Exception
     */
    @PostMapping("/download")
    public void download(HttpServletResponse response) throws Exception {
        response.setContentType("Image/png");
        InputStream in = minioClient.getObject(GetObjectArgs.builder().bucket(TEST).object(list.pop()).build());
        //字节输出流
        ServletOutputStream out = response.getOutputStream();

        // 存储读取到的数据
        int read = 0;
        while ((read = in.read()) != -1) {
        // 将读取到的数据输出
            response.getOutputStream().write(read);
        }
        // 关闭流
        response.getOutputStream().close();
        in.close();
    }

}
