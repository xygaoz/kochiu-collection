package com.keem.kochiu.collection.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImportProgressWebSocketHandler extends TextWebSocketHandler {

    // 存储任务ID与WebSocketSession的映射
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String taskId = session.getHandshakeHeaders().getFirst("task-id");
        if (taskId != null) {
            sessions.put(taskId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session); // 连接关闭时移除
    }

    // 发送进度更新（供其他服务调用）
    public static void sendProgress(String taskId, ImportProgress progress) {
        WebSocketSession session = sessions.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(progress)));
            } catch (IOException e) {
                sessions.remove(taskId); // 发送失败时清理
            }
        }
    }

    // 进度数据模型
    @Data
    @AllArgsConstructor
    public static class ImportProgress {
        private int current;
        private int total;
        private String currentFile;
        private String status; // "processing", "completed", "error"
        private String errorMessage;
    }
}