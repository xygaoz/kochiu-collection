package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.bo.PathBo;
import com.kochiu.collection.data.dto.StrategyDto;
import com.kochiu.collection.entity.SysStrategy;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.ImportMethodEnum;
import com.kochiu.collection.enums.StrategyEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

import static com.kochiu.collection.Constant.ROOT_PATH;
import static com.kochiu.collection.Constant.ROOT_PATH_WIN;

@Slf4j
@Service
public class SysStrategyService {

    private final SysStrategyRepository strategyRepository;
    private final SystemService systemService;

    public SysStrategyService(SysStrategyRepository strategyRepository,
                              SystemService systemService) {
        this.strategyRepository = strategyRepository;
        this.systemService = systemService;
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

    // 更新系统策略
    @Transactional(rollbackFor = Exception.class)
    public void updateStrategy(StrategyDto strategyDto) throws CollectionException {

        SysStrategy sysStrategy = strategyRepository.getById(strategyDto.getStrategyId());
        if (sysStrategy == null) {
            throw new CollectionException(ErrorCodeEnum.STRATEGY_IS_NOT_EXIST);
        }

        if(sysStrategy.getStrategyCode().equals(StrategyEnum.LOCAL.getCode())
                && !sysStrategy.getServerUrl().equals(strategyDto.getServerUrl())
                && !systemService.testServerPath(PathBo.builder()
                    .path(sysStrategy.getServerUrl())
                .importMethod(ImportMethodEnum.MOVE)
                .build())){
            throw new CollectionException(ErrorCodeEnum.SERVER_PATH_ERROR);
        }

        String oldPath = sysStrategy.getServerUrl();
        sysStrategy.setServerUrl(strategyDto.getServerUrl());
        sysStrategy.setUsername(strategyDto.getUsername());
        sysStrategy.setPassword(strategyDto.getPassword());
        sysStrategy.setOtherConfig(strategyDto.getOtherConfig());
        strategyRepository.updateById(sysStrategy);

        if(sysStrategy.getStrategyCode().equals(StrategyEnum.LOCAL.getCode())
                && !oldPath.equals(strategyDto.getServerUrl())
                && !oldPath.equals(ROOT_PATH)
                && !oldPath.equalsIgnoreCase(ROOT_PATH_WIN)){
            //迁移文件
            Path dir = Paths.get(oldPath);
            if(!dir.toFile().exists()){
                return;
            }

            boolean isPresent;
            try (Stream<Path> stream = Files.list(dir)) {
                isPresent = stream.findAny().isPresent();
            } catch (IOException e) {
                log.error("Failed to list directory: {}", dir, e);
                isPresent = true;
            }

            // 如果存在文件，则迁移文件
            if(isPresent){
                //判断目标路径是否存在
                try {
                    Path target = Paths.get(sysStrategy.getServerUrl());
                    if (Paths.get(sysStrategy.getServerUrl()).toFile().exists()) {
                        //迁移文件
                        // 使用 Files.walkFileTree 递归遍历源目录
                        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                Path targetDir = target.resolve(dir.relativize(dir));
                                if (!Files.exists(targetDir)) {
                                    Files.createDirectories(targetDir);
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
                                Path targetFile = target.resolve(dir.relativize(sourceFile));
                                Path parentDir = targetFile.getParent();

                                // 显式创建父目录（双重保险）
                                if (!Files.exists(parentDir)) {
                                    Files.createDirectories(parentDir);
                                }

                                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                                System.err.println("处理文件失败: " + file + " | 错误: " + e.getMessage());
                                return FileVisitResult.CONTINUE; // 跳过错误继续复制
                            }
                        });
                        //删除源目录
                        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file); // 删除文件
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                if (exc != null) {
                                    throw exc; // 抛出遍历过程中的异常
                                }
                                Files.delete(dir); // 删除目录（此时目录已空）
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } else {
                        //直接改名
                        Files.move(dir, target);
                    }
                }
                catch (Exception e) {
                    log.error("Failed to copy directory: {}", dir, e);
                    throw new CollectionException(ErrorCodeEnum.MIGRATION_FAILED);
                }
            }
        }
    }

    // 检查本地策略
    public boolean checkLocalStrategy() {
        SysStrategy sysStrategy = strategyRepository.getOne(new LambdaQueryWrapper<SysStrategy>().eq(SysStrategy::getStrategyCode, StrategyEnum.LOCAL.getCode()));
        if (sysStrategy != null) {
            if(ROOT_PATH.equals(sysStrategy.getServerUrl()) || sysStrategy.getServerUrl().toUpperCase().startsWith(ROOT_PATH_WIN)){
                log.error("不能存储到根目录");
                return false;
            }

            Path dir = Paths.get(sysStrategy.getServerUrl());
            if(!dir.toFile().exists()){
                log.error("{} 目录不存在", sysStrategy.getServerUrl());
                return false;
            }

            try {
                File file = dir.toFile();
                if (file.isDirectory()) {
                    // 测试读权限
                    file.list();
                    // 测试写权限
                    File testFile = new File(file, ".kochiu_test_" + System.currentTimeMillis());
                    boolean created = testFile.createNewFile();
                    if (created) {
                        testFile.delete();
                    }
                    else{
                        log.error("存储路径不可读写");
                    }
                    return created;
                }
                log.error("存储路径不是目录");
                return false;
            } catch (Exception e) {
                log.error("存储路径异常", e);
                return false;
            }
        }
        log.error("本地存储策略找不到");
        return false;
    }
}
