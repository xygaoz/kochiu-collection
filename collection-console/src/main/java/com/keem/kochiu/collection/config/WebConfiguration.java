package com.keem.kochiu.collection.config;

import com.keem.kochiu.collection.enums.AutoCreateRuleEnum;
import com.keem.kochiu.collection.enums.ImportMethodEnum;
import com.keem.kochiu.collection.enums.RemoveUserOptionEnum;
import com.keem.kochiu.collection.properties.CollectionProperties;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.keem.kochiu.collection.enums.RemoveEnum;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final CollectionProperties collectionProperties;

    public WebConfiguration(CollectionProperties collectionProperties) {
        this.collectionProperties = collectionProperties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(getClientHttpRequestFactory());
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = collectionProperties.getJodconverter().getRemote().getTimeout(); // 5分钟（单位：毫秒）
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)      // 连接超时
                .setConnectionRequestTimeout(timeout) // 请求超时
                .setSocketTimeout(timeout)       // 响应读取超时
                .build();

        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册 String -> RemoveEnum 的转换器
        registry.addConverter(new StringToRemoveEnumConverter());
        registry.addConverter(new StringToAutoCreateRuleEnumConverter());
        registry.addConverter(new StringToImportMethodEnumConverter());
        registry.addConverter(new StringToRemoveUserOptionEnumConverter());
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
}