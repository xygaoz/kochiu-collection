package com.keem.kochiu.collection.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class ImportTaskService {
    // 存储所有任务的 Future 引用（用于取消）
    private final Map<String, Future<?>> taskFutures = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public String submitTask(Runnable task) {
        String taskId = UUID.randomUUID().toString();
        Future<?> future = executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("任务执行失败: {}", taskId, e);
            } finally {
                taskFutures.remove(taskId); // 任务完成或异常时清理
            }
        });
        taskFutures.put(taskId, future);
        return taskId;
    }


    // 取消任务
    public boolean cancelTask(String taskId) {
        Future<?> future = taskFutures.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(true); // true 表示中断正在执行的任务
            taskFutures.remove(taskId);
            return cancelled;
        }
        return false;
    }

    // 清理资源（可选）
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}