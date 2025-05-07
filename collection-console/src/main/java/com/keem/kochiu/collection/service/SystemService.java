package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.bo.PathBo;
import com.keem.kochiu.collection.data.dto.StrategyDto;
import com.keem.kochiu.collection.entity.SysStrategy;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.enums.ImportMethodEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysStrategyRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SystemService {

    private final SysStrategyRepository strategyRepository;

    // Linux系统敏感目录
    private static final Set<String> LINUX_SENSITIVE_DIRS = new HashSet<>(Arrays.asList(
            "/bin", "/boot", "/dev", "/etc", "/home", "/lib", "/lib64",
            "/lost+found", "/media", "/mnt", "/opt", "/proc", "/root",
            "/run", "/sbin", "/srv", "/sys", "/tmp", "/usr", "/var"
    ));

    // Windows系统敏感目录
    private static final Set<String> WINDOWS_SENSITIVE_DIRS = new HashSet<>(Arrays.asList(
            "C:\\Windows", "C:\\Program Files", "C:\\Program Files (x86)",
            "C:\\ProgramData", "C:\\System Volume Information", "C:\\$Recycle.Bin",
            "C:\\Users", "C:\\Documents and Settings", "C:\\PerfLogs",
            "C:\\Recovery", "C:\\Config.Msi", "C:\\MSOCache"
    ));

    public SystemService(SysStrategyRepository strategyRepository) {
        this.strategyRepository = strategyRepository;
    }

    /**
     * 测试服务端路径是否可读写
     * @param pathBo 要测试的路径
     * @return 如果路径安全且可读写返回true，否则返回false
     */
    public boolean testServerPath(PathBo pathBo) {
        String path = pathBo.getPath();
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        // 规范化路径
        Path normalizedPath;
        try {
            normalizedPath = Paths.get(path).normalize().toAbsolutePath();
        } catch (Exception e) {
            return false;
        }

        // 检查是否包含敏感目录
        if (isSensitivePath(normalizedPath.toString())) {
            return false;
        }

        // 检查路径是否存在且可读写
        File file = normalizedPath.toFile();
        if (!file.exists()) {
            // 尝试创建目录测试可写性
            try {
                boolean created = file.mkdirs();
                if (created) {
                    file.delete(); // 删除测试创建的目录
                    return true;
                }
                return false;
            } catch (SecurityException e) {
                return false;
            }
        } else {
            // 检查现有目录的可读写性
            try {
                if (file.isDirectory()) {
                    // 测试读权限
                    file.list();
                    // 测试写权限
                    if(pathBo.getImportMethod() == ImportMethodEnum.MOVE) {
                        File testFile = new File(file, ".kochiu_test_" + System.currentTimeMillis());
                        boolean created = testFile.createNewFile();
                        if (created) {
                            testFile.delete();
                        }
                        return created;
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * 检查路径是否包含敏感系统目录
     * @param path 要检查的路径
     * @return 如果是敏感路径返回true，否则返回false
     */
    private boolean isSensitivePath(String path) {
        // 统一转换为小写进行比较
        String lowerPath = path.toLowerCase();

        // 检查Linux系统目录
        if (File.separatorChar == '/') { // Linux系统
            for (String dir : LINUX_SENSITIVE_DIRS) {
                if (lowerPath.startsWith(dir.toLowerCase() + "/")) {
                    return true;
                }
            }
        } else { // Windows系统
            for (String dir : WINDOWS_SENSITIVE_DIRS) {
                if (lowerPath.startsWith(dir.toLowerCase() + "\\")) {
                    return true;
                }
            }
        }

        // 检查常见的敏感路径模式
        if (lowerPath.contains("system32") ||
                lowerPath.contains("syswow64") ||
                lowerPath.contains("etc") ||
                lowerPath.contains("var/log")) {
            return true;
        }

        return false;
    }

    // 获取系统策略列表
    public List<StrategyDto> getStrategyList() {
        return strategyRepository
                .list()
                .stream()
                .map(strategy ->
                        StrategyDto.builder()
                                .strategyId(strategy.getStrategyId())
                                .strategyCode(strategy.getStrategyCode())
                                .strategyName(strategy.getStrategyName())
                                .serverUrl(strategy.getServerUrl())
                                .username(strategy.getUsername())
                                .password(strategy.getPassword())
                                .otherConfig(strategy.getOtherConfig())
                                .build())
                .toList();
    }

    public void updateStrategy(StrategyDto strategyDto) throws CollectionException {

        SysStrategy  sysStrategy = strategyRepository.getById(strategyDto.getStrategyId());
        if(sysStrategy == null){
            throw new CollectionException(ErrorCodeEnum.STRATEGY_IS_NOT_EXIST);
        }
        sysStrategy.setServerUrl(strategyDto.getServerUrl());
        sysStrategy.setUsername(strategyDto.getUsername());
        sysStrategy.setPassword(strategyDto.getPassword());
        sysStrategy.setOtherConfig(strategyDto.getOtherConfig());
        strategyRepository.updateById(sysStrategy);
    }
}