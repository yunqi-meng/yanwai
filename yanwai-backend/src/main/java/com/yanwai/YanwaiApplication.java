package com.yanwai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class YanwaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YanwaiApplication.class, args);
    }
}
