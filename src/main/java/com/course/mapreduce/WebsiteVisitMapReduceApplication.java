package com.course.mapreduce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.course.mapreduce.mapper")
public class WebsiteVisitMapReduceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsiteVisitMapReduceApplication.class, args);
    }
}
