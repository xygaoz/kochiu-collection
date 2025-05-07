package com.keem.kochiu.collection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class ImportTaskService {
    // 存储所有任务的 Future 引用（用于取消）
    private final Map<String, Future<?>> taskFutures = new ConcurrentHashMap<>();
    // 存储用户与任务的映射关系（key: userId, value: taskId）
    private final Map<Integer, String> userTaskMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void submitTask(Integer userId, String taskId, Runnable task) {
        // 检查用户是否已有任务在执行
        if (userTaskMap.containsKey(userId)) {
            throw new IllegalStateException("用户已有任务在执行中");
        }

        Future<?> future = executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("任务执行失败: {}", taskId, e);
            } finally {
                taskFutures.remove(taskId); // 任务完成或异常时清理
                userTaskMap.remove(userId);
            }
        });
        taskFutures.put(taskId, future);
        userTaskMap.put(userId, taskId);
    }


    // 取消任务
    public boolean cancelTask(String taskId) {
        Future<?> future = taskFutures.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(true); // true 表示中断正在执行的任务
            taskFutures.remove(taskId);
            // 从用户映射中移除
            userTaskMap.values().removeIf(taskId::equals);
            return cancelled;
        }
        return false;
    }

    // 检查用户是否有任务在执行
    public boolean hasRunningTask(Integer userId) {
        return userTaskMap.containsKey(userId);
    }

    // 清理资源（可选）
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}