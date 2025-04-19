package com.keem.kochiu.collection;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author KoChiu
 */
@Slf4j
@SpringBootApplication
@EnableScheduling //开启定时任务功能
@EnableTransactionManagement
@MapperScan("com.keem.kochiu.collection.mapper")
public class Application extends SpringBootServletInitializer {

    /**
     * IDE运行时 运行此函数
     *
     * @param args
     */
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        log.info("启动完成～～");
    }

}


