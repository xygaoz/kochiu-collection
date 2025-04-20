package com.keem.kochiu.collection.config;

import com.keem.kochiu.collection.enums.AutoCreateRuleEnum;
import com.keem.kochiu.collection.enums.ImportMethodEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.keem.kochiu.collection.enums.RemoveEnum;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册 String -> RemoveEnum 的转换器
        registry.addConverter(new StringToRemoveEnumConverter());
        registry.addConverter(new StringToAutoCreateRuleEnumConverter());
        registry.addConverter(new StringToImportMethodEnumConverter());
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
}