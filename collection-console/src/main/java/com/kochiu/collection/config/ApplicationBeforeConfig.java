package com.kochiu.collection.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.kochiu.collection.Constant.*;

@Slf4j
public class ApplicationBeforeConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * 智能获取数据库路径（完美兼容所有环境）
     */
    private static String getDbPath() {
        // 1. 环境变量优先
        String envPath = System.getenv(DB_PATH_PROPERTY);
        if (StringUtils.isNotBlank(envPath)) {
            return envPath;
        }

        // 2. 检查标准路径（兼容开发和生产）
        List<String> possiblePaths = Arrays.asList(
                System.getProperty("user.dir") + File.separator + "db",  // 开发环境
                CONTAINER_DB_PATH,                                              // Docker默认路径
                CONTAINER_BACKUP_DB_PATH,                                      // 容器备用路径
                System.getProperty("java.io.tmpdir") + File.separator + "db" // 系统临时目录
        );

        // 返回第一个存在的路径，否则创建第一个路径
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() || dir.mkdirs()) {
                return path;
            }
        }
        throw new IllegalStateException("无法确定数据库存储路径");
    }


    /**
     * 初始化h2数据库
     */
    public synchronized static void initDB() throws IOException {

        String dbPath = getDbPath();
        log.info("数据库路径：{}", dbPath);
        Path dbDir = Paths.get(dbPath);

        // 创建目录（如果不存在）
        if (!Files.exists(dbDir)) {
            Files.createDirectories(dbDir);
            log.info("创建数据库目录：{}", dbDir.toAbsolutePath());
        }
        else{
            log.info("数据库目录已存在：{}", dbDir.toAbsolutePath());
        }
        System.setProperty("DB_PATH", dbPath);
        log.info("DB_PATH: {}", System.getProperty("DB_PATH"));

        // 初始化数据库文件
        Path dbFile = dbDir.resolve("collection.db");
        if (!Files.exists(dbFile)) {
            ClassPathResource sourceDb = new ClassPathResource("/db/simple.db");
            if (sourceDb.exists()) {
                try (InputStream is = sourceDb.getInputStream()) {
                    Files.copy(is, dbFile);
                    // 确保文件可写（解决容器权限问题）
                    dbFile.toFile().setWritable(true, false);
                    log.info("初始化数据库文件：{}", dbFile.toAbsolutePath());
                }
            }
            else{
                log.error("系统空数据库文件不存在：{}", sourceDb.getPath());
            }
        }
        else{
            log.info("数据库文件已存在：{}", dbFile.toAbsolutePath());
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            log.info("【开始】初始化数据库");
            initDB();
            log.info("环境变量DB_PATH: {}", System.getenv("DB_PATH"));
            log.info("系统属性DB_PATH: {}", System.getProperty("DB_PATH"));
            log.info("【结束】初始化数据库");
        } catch (IOException e) {
            log.error("初始化数据库失败", e);
        }
    }
}

