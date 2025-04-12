package com.keem.kochiu.collection.config;

import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * 拦截预编译语句输出完整SQL
 */
@Slf4j
@Component
@Intercepts(
        {
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
        }
)
public class SqlLogConfig implements Interceptor {

    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];

        String type = statement.getSqlCommandType().toString();

        Field field = MappedStatement.class.getDeclaredField("sqlSource");
        field.setAccessible(true);
        SqlSource sqlSource = (SqlSource) field.get(statement);
        BoundSql bound = sqlSource.getBoundSql(invocation.getArgs()[1]);

        Configuration configuration = statement.getConfiguration();
        String sql = getFullSql(configuration, bound);

        long start = System.currentTimeMillis();
        String statementId = statement.getId();

        try {
            Object value = invocation.proceed();
            long end = System.currentTimeMillis();
            if("UPDATE".equals(type) || "INSERT".equals(type)) {
                log.info("SQL执行类: {} \n完整SQL: {}\n插入/更新: {}\nSQL耗时: {}ms", statementId, sql, value, (end - start));
            }
            else{
                log.info("SQL执行类: {} \n完整SQL: {}\nSQL耗时: {}ms", statementId, sql, (end - start));
            }
            return value;
        }
        catch (Exception e){
            log.info("SQL执行类: {} \n完整SQL: {}\n", statementId, sql);
            throw e;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过MyBatis配置文件或注解传递属性
    }

    public String getFullSql(Configuration conf, BoundSql bound) {
        Object object = bound.getParameterObject();
        List<ParameterMapping> list = bound.getParameterMappings();
        String sql = bound.getSql().replaceAll("[\\s]+", " ").toLowerCase(Locale.ROOT);
        if (CollUtil.isNotEmpty(list) && object != null) {
            TypeHandlerRegistry type = conf.getTypeHandlerRegistry();
            if (type.hasTypeHandler(object.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParaValue(object)));
            } else {
                MetaObject meta = conf.newMetaObject(object);
                for (ParameterMapping parameterMapping : list) {
                    String name = parameterMapping.getProperty();
                    if (meta.hasGetter(name)) {
                        Object obj = meta.getValue(name);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParaValue(obj)));
                    } else if (bound.hasAdditionalParameter(name)) {
                        Object obj = bound.getAdditionalParameter(name);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParaValue(obj)));
                    } else {
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    private String getParaValue(Object obj) {
        if (obj instanceof String) {
            return "'" + obj + "'";
        } else if (obj instanceof Date) {
            return "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                return obj.toString();
            } else {
                return "null";
            }
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class TaskInfo{
        private String taskCode;
        private String mcCode;
    }
}
