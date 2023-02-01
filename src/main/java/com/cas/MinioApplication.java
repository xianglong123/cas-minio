package com.cas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: xianglong[1391086179@qq.com]
 * @date: 上午10:26 2023/02/01
 * @version: V1.0
 */
@SpringBootApplication(scanBasePackages = "com.cas")
public class MinioApplication {

    public static void main(String[] args) {
        try{
            SpringApplication.run(MinioApplication.class, args);
            System.out.println("启动成功！！！！");
        } catch (Exception e) {
            System.out.println("启动失败！！！！");
        }
    }

}
