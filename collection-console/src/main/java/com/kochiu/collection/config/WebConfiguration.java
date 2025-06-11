package com.kochiu.collection.config;

import com.kochiu.collection.enums.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    // 解决 history 模式 404 问题
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册 String -> RemoveEnum 的转换器
        registry.addConverter(new StringToRemoveEnumConverter());
        registry.addConverter(new StringToAutoCreateRuleEnumConverter());
        registry.addConverter(new StringToImportMethodEnumConverter());
        registry.addConverter(new StringToRemoveUserOptionEnumConverter());
        registry.addConverter(new StringToCategoryByEnumConverter());
        registry.addConverter(new StringToTagByEnumConverter());
    }

    public static class StringToRemoveEnumConverter implements Converter<String, RemoveEnum> {
        @Override
        public RemoveEnum convert(@Nullable String source) {
            return RemoveEnum.fromCode(source); // 调用枚举的 fromCode 方法
        }
    }

    public static class StringToAutoCreateRuleEnumConverter implements Converter<String, AutoCreateRuleEnum> {
        @Override
        public AutoCreateRuleEnum convert(@Nullable String source) {
            return AutoCreateRuleEnum.getByCode(source); // 调用枚举的 fromCode 方法
        }
    }

    public static class StringToImportMethodEnumConverter implements Converter<String, ImportMethodEnum> {
        @Override
        public ImportMethodEnum convert(@Nullable String source) {
            return ImportMethodEnum.getByCode(source); // 调用枚举的 fromCode 方法
        }
    }

    public static class StringToRemoveUserOptionEnumConverter implements Converter<String, RemoveUserOptionEnum> {
        @Override
        public RemoveUserOptionEnum convert(@Nullable String source) {
            return RemoveUserOptionEnum.getByValue(source); // 调用枚举的 fromCode 方法
        }
    }

    public static class StringToCategoryByEnumConverter implements Converter<String, CategoryByEnum> {
        @Override
        public CategoryByEnum convert(@Nullable String source) {
            return CategoryByEnum.getByCode(NumberUtils.toInt(source, 1)); // 调用枚举的 fromCode 方法
        }
    }
    public static class StringToTagByEnumConverter implements Converter<String, TagByEnum> {
        @Override
        public TagByEnum convert(@Nullable String source) {
            return TagByEnum.getByCode(NumberUtils.toInt(source, 1)); // 调用枚举的 fromCode 方法
        }
    }
}