package com.example.qing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.qing.mapper")
public class QingApplication {
    public static void main(String[] args) {
        SpringApplication.run(QingApplication.class,args);
    }
}
