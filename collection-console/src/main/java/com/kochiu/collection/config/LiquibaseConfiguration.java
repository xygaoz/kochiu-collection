package com.kochiu.collection.config;

import com.kochiu.collection.properties.SysConfigProperties;
import com.kochiu.collection.properties.UserConfigProperties;
import com.kochiu.collection.repository.SysConfigRepository;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserConfigRepository;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 *
 * @author KoChiu
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class LiquibaseConfiguration {

    protected static final String LIQUIBASE_CHANGELOG_PREFIX = "KC_DB_";
    private final JdbcTemplate jdbcTemplate;

    public LiquibaseConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 在应用启动后执行以下代码检查
    @PostConstruct
    public void checkWalMode() {
        log.info("Checking WAL mode: {}", jdbcTemplate.queryForObject("PRAGMA journal_mode", String.class));
    }

    @Bean("KoChiuCollection")
    @Qualifier("KoChiuCollection")
    public Liquibase producerLiquibase(DataSource dataSource) throws DatabaseException {
        log.info("Configuring Liquibase");

        Liquibase liquibase = null;
        DatabaseConnection connection = null;
        try {
            connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            database.setDatabaseChangeLogTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogTableName());
            database.setDatabaseChangeLogLockTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogLockTableName());

            liquibase = new Liquibase("META-INF/liquibase/collection-db-changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("kochiu-collection-producer");

            return liquibase;
        } catch (Exception e) {
            throw new DatabaseException("Error creating liquibase database", e);
        } finally {
            closeDatabase(liquibase);
            if(connection != null && !connection.isClosed()){
                connection.close();
            }
        }
    }

    private void closeDatabase(Liquibase liquibase) {
        if (liquibase != null) {
            Database database = liquibase.getDatabase();
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    log.warn("Error closing database", e);
                }
            }
        }
    }

    @DependsOn("KoChiuCollection")
    @Bean
    public SysConfigProperties sysConfigProperties(SysConfigRepository sysConfigRepository) {
        SysConfigProperties properties = new SysConfigProperties(sysConfigRepository);
        try {
            properties.afterPropertiesSet();
        } catch (Exception e) {
            log.error("SysConfigProperties init error", e);
        }
        return properties;
    }

    @DependsOn("KoChiuCollection")
    @Bean
    public UserConfigProperties userConfigProperties(UserConfigRepository userConfigRepository,
                                                     SysUserRepository sysUserRepository) {
        UserConfigProperties properties = new UserConfigProperties(userConfigRepository, sysUserRepository);
        try {
            properties.afterPropertiesSet();
        } catch (Exception e) {
            log.error("UserConfigProperties init error", e);
        }
        return properties;
    }
}
