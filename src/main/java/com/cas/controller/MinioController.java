package com.cas.controller;

import com.cas.util.MinIOUtils;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
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
import java.util.concurrent.TimeUnit;

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
        list.push("/1012/T/B/" + filename);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(TEST)
                        .object("/1012/T/B/" + filename)
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

    /**
     * 获取所有文件名
     *
     * @throws Exception
     */
    @PostMapping("/query")
    public List<String> query() throws Exception {
        List<String> fileNames = new ArrayList<>();

        // 列出存储桶中的所有对象
        Iterable<io.minio.Result<io.minio.messages.Item>> results =
                minioClient.listObjects(
                        io.minio.ListObjectsArgs.builder()
                                .bucket(TEST)
                                .build());

        // 遍历结果并提取文件名
        for (io.minio.Result<io.minio.messages.Item> result : results) {
            io.minio.messages.Item item = result.get();
            fileNames.add(item.objectName());
        }

        return fileNames;
    }

    /**
     * 获取上传文件地址
     *
     * @throws Exception
     * http://127.0.0.1:9000/test/example.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin%2F20250724%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250724T054622Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=a7b255e53f9a2339a9ab4b1d604c59d8d36e329b5e581a444498e9a354f47902
     */
    @PostMapping("/queryUploadUrl")
    public String queryUploadUrl() throws Exception {
        String uploadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(TEST)
                        .object("example.jpg")
                        .expiry(7, TimeUnit.DAYS)
                        .build());
        System.out.println("上传URL: " + uploadUrl);
        return uploadUrl;
    }

}
