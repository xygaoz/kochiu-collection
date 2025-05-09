package com.kochiu.collection.config;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Configuration
public class ApplicationBeforeConfig implements BeanPostProcessor {
    /**
     * 初始化h2数据库
     */
    public synchronized static void initDB() throws IOException {
        String projectPath = System.getProperty("user.dir");
        String dbHomePath = projectPath + File.separator + "db";
        File fileDbHomePath = new File(dbHomePath);
        if (!fileDbHomePath.exists()) {
            FileUtil.mkdir(dbHomePath);
            //判断是否存在db文件
            ClassPathResource classPathResource = new ClassPathResource("/db/simple.db");
            if(classPathResource.exists()) {
                IOUtils.copy(classPathResource.getInputStream(), Files.newOutputStream(new File(dbHomePath, "collection.db").toPath()));
            }
        }
    }

    @PostConstruct
    public void test() {
        try {
            log.info("【开始】初始化数据库");
            initDB();
            log.info("【结束】初始化数据库");
        } catch (IOException e) {
            log.error("初始化数据库失败", e);
        }
    }
}

