package com.keem.kochiu.collection.config;

import com.keem.kochiu.collection.handler.ImportProgressWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(importProgressWebSocketHandler(), "/ws/import-progress")
                .setAllowedOrigins("*"); // 允许跨域
    }

    @Bean
    public ImportProgressWebSocketHandler importProgressWebSocketHandler() {
        return new ImportProgressWebSocketHandler();
    }
}