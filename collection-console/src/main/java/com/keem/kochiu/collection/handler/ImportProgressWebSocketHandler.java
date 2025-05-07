package com.keem.kochiu.collection.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ImportProgressWebSocketHandler extends TextWebSocketHandler {

    // 存储任务ID与WebSocketSession的映射
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String taskId = getTaskIdFromSession(session); // 从请求参数获取taskId
        sessions.put(taskId, session);
        log.info("WebSocket 连接建立: taskId={}", taskId);
    }

    private String getTaskIdFromSession(WebSocketSession session) {
        // 从URL参数中提取taskId，例如: /ws/import-progress?task-id=123
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        return query.split("=")[1];
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.values().remove(session); // 连接关闭时移除
    }

    // 发送进度更新（供其他服务调用）
    public static void sendProgress(String taskId, Object progress) {
        WebSocketSession session = sessions.get(taskId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(progress);
                session.sendMessage(new TextMessage(message));
                log.debug("消息已发送: taskId={}, message={}", taskId, message); // 关键日志
            } catch (IOException e) {
                log.error("发送消息失败", e);
                sessions.remove(taskId);
            }
        }
    }

    // 发送取消通知
    public static void sendCancelled(String taskId) {
        WebSocketSession session = sessions.get(taskId);
        if (session != null) {
            try {
                ImportProgress progress = new ImportProgress(
                        -1, -1, -1, -1, "", "cancelled", "任务已被用户取消"
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(progress)));
                session.close(); // 关闭连接
            } catch (IOException e) {
                sessions.remove(taskId);
            }
        }
    }

    // 进度数据模型
    @Data
    @AllArgsConstructor
    public static class ImportProgress {
        private int current;
        private int total;
        private int success;
        private int fail;
        private String currentFile;
        private String status; // "processing", "completed", "error"
        private String errorMessage;
    }
}