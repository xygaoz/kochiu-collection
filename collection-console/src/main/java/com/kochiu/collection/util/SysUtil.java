package com.kochiu.collection.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SysUtil {

    public static boolean isRunningInDocker() {
        // 方法1: 检查.dockerenv文件
        if (new File("/.dockerenv").exists()) {
            return true;
        }

        // 方法2: 检查cgroup
        try {
            String cgroup = Files.readString(Paths.get("/proc/1/cgroup"));
            if (cgroup.contains("docker") || cgroup.contains("kubepods")) {
                return true;
            }
        } catch (IOException ignored) {}

        // 方法3: 检查环境变量
        return System.getenv().keySet().stream()
                .anyMatch(key -> key.toLowerCase().contains("docker"));
    }
}
