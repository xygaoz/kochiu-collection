package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.bo.PathBo;
import com.kochiu.collection.data.vo.KeyVo;
import com.kochiu.collection.entity.*;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.ImportMethodEnum;
import com.kochiu.collection.enums.StrategyEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.properties.SysConfigProperties;
import com.kochiu.collection.repository.*;
import com.kochiu.collection.util.DesensitizationUtil;
import com.kochiu.collection.util.RsaHexUtil;
import com.kochiu.collection.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.kochiu.collection.Constant.RANDOM_CHARS;

@Slf4j
@Service
public class SystemService {

    private final ApplicationContext applicationContext;
    private final UserCatalogRepository userCatalogRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final UserResourceRepository userResourceRepository;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserResourceTagRepository userResourceTagRepository;
    private final UserTagRepository userTagRepository;
    private final UserRoleRepository userRoleRepository;
    private final SysStrategyRepository strategyRepository;
    private final SysSecurityRepository securityRepository;
    private final SysConfigProperties sysConfigProperties;
    private final SysConfigRepository sysConfigRepository;

    // Linux系统敏感目录
    private static final Set<String> LINUX_SENSITIVE_DIRS = new HashSet<>(Arrays.asList(
            "/", "/bin", "/boot", "/dev", "/etc", "/home", "/lib", "/lib64",
            "/lost+found", "/media", "/mnt", "/opt", "/proc", "/root",
            "/run", "/sbin", "/srv", "/sys", "/tmp", "/usr", "/var"
    ));

    // Windows系统敏感目录
    private static final Set<String> WINDOWS_SENSITIVE_DIRS = new HashSet<>(Arrays.asList(
            "/", "C:", "C:\\Windows", "C:\\Program Files", "C:\\Program Files (x86)",
            "C:\\ProgramData", "C:\\System Volume Information", "C:\\$Recycle.Bin",
            "C:\\Users", "C:\\Documents and Settings", "C:\\PerfLogs",
            "C:\\Recovery", "C:\\Config.Msi", "C:\\MSOCache"
    ));

    public SystemService(ApplicationContext applicationContext,
                         UserCatalogRepository userCatalogRepository,
                         UserCategoryRepository userCategoryRepository,
                         UserResourceRepository userResourceRepository,
                         SysUserRepository sysUserRepository,
                         SysRoleRepository sysRoleRepository,
                         UserPermissionRepository userPermissionRepository,
                         UserResourceTagRepository userResourceTagRepository,
                         UserTagRepository userTagRepository,
                         UserRoleRepository userRoleRepository,
                         SysStrategyRepository strategyRepository,
                         SysSecurityRepository securityRepository,
                         SysConfigProperties sysConfigProperties,
                         SysConfigRepository sysConfigRepository) {
        this.applicationContext = applicationContext;
        this.userCatalogRepository = userCatalogRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.userResourceRepository = userResourceRepository;
        this.sysUserRepository = sysUserRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.userResourceTagRepository = userResourceTagRepository;
        this.userTagRepository = userTagRepository;
        this.userRoleRepository = userRoleRepository;
        this.strategyRepository = strategyRepository;
        this.securityRepository = securityRepository;
        this.sysConfigProperties = sysConfigProperties;
        this.sysConfigRepository = sysConfigRepository;
    }

    /**
     * 测试服务端路径是否可读写
     *
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
                    if (pathBo.getImportMethod() == ImportMethodEnum.MOVE) {
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
     *
     * @param path 要检查的路径
     * @return 如果是敏感路径返回true，否则返回false
     */
    public boolean isSensitivePath(String path) {
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

    //  清空数据
    @Transactional(rollbackFor = Exception.class)
    public void clear() throws CollectionException {
        //清空标签
        userResourceTagRepository.remove(null);
        //清空资源
        userResourceRepository.remove(null);
        //清空标签
        userTagRepository.remove(null);
        //清空用户
        sysUserRepository.remove(new LambdaQueryWrapper<SysUser>().eq(SysUser::getCanDel, 1));
        //清空角色
        sysRoleRepository.remove(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCanDel, 1));
        //清空用户角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.notInSql(UserRole::getUserId, "SELECT user_id FROM sys_user");
        userRoleRepository.remove(wrapper);
        //清空权限
        LambdaQueryWrapper<UserPermission> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.notInSql(UserPermission::getRoleId, "SELECT role_id FROM sys_role");
        userPermissionRepository.remove(wrapper2);
        //清空目录
        LambdaQueryWrapper<UserCatalog> wrapper3 = new LambdaQueryWrapper<>();
        wrapper3.notInSql(UserCatalog::getUserId, "SELECT user_id FROM sys_user");
        wrapper3.or(subWrapper -> {
            subWrapper.inSql(UserCatalog::getUserId, "SELECT user_id FROM sys_user");
            subWrapper.ne(UserCatalog::getCataLevel, 0);
        });
        userCatalogRepository.remove(wrapper3);
        //清空分类
        LambdaQueryWrapper<UserCategory> wrapper4 = new LambdaQueryWrapper<>();
        wrapper4.notInSql(UserCategory::getUserId, "SELECT user_id FROM sys_user");
        wrapper4.or(subWrapper -> {
            subWrapper.inSql(UserCategory::getUserId, "SELECT user_id FROM sys_user");
            subWrapper.ne(UserCategory::getSno, 1);
        });
        userCategoryRepository.remove(wrapper4);
        //删除物理资源
        try {
            SysStrategy sysStrategy = strategyRepository.getOne(new LambdaQueryWrapper<SysStrategy>().eq(SysStrategy::getStrategyCode, StrategyEnum.LOCAL.getCode()));
            if (sysStrategy != null) {
                Path dir = Paths.get(sysStrategy.getServerUrl());
                if (!dir.toFile().exists()) {
                    return;
                }
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
                        if(!SysUtil.isRunningInDocker()) {
                            //容器运行不能删映射目录
                            Files.delete(dir); // 删除目录（此时目录已空）
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            log.error("Failed to delete directory", e);
            throw new CollectionException(ErrorCodeEnum.CLEAR_FAIL);
        }
    }

    // 获取密钥
    public KeyVo getKeys() {

        SysSecurity sysSecurity = securityRepository.getById(1);
        KeyVo keyVo = new KeyVo();
        keyVo.setPublicKey(sysSecurity.getPublicKey());
        keyVo.setPrivateKey(sysSecurity.getPrivateKey().substring(0, 60) + "……～～已隐藏" + (sysSecurity.getPrivateKey().length() - 120) + "字符～～……" + sysSecurity.getPrivateKey().substring(sysSecurity.getPrivateKey().length() - 60));
        keyVo.setCommonKey(DesensitizationUtil.mask(sysSecurity.getCommonKey(), 2, sysSecurity.getCommonKey().length() - 3, '*'));
        return keyVo;
    }

    // 重置RSA密钥
    @Transactional(rollbackFor = Exception.class)
    public void resetRsaKeys() throws CollectionException {
        try {
            String[] keys = RsaHexUtil.genKeyPair();
            SysSecurity sysSecurity = securityRepository.getById(1);
            sysSecurity.setPublicKey(keys[0]);
            sysSecurity.setPrivateKey(keys[1]);
            securityRepository.updateById(sysSecurity);
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.RSA_KEY_GEN_FAIL);
        }
    }

    //  重置公用密钥
    @Transactional(rollbackFor = Exception.class)
    public void resetCommonKey() throws CollectionException {
        try {
            String commonKey = RandomStringUtils.random(16, RANDOM_CHARS);
            SysSecurity sysSecurity = securityRepository.getById(1);
            sysSecurity.setCommonKey(commonKey);
            securityRepository.updateById(sysSecurity);
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.RSA_KEY_GEN_FAIL);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setSysConfig(SysConfigProperties.SysProperty property) {

        // 保存配置到数据库
        sysConfigRepository.remove(null);
        Map<String, String> configMap = property.toMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            SysConfig sysConfig = new SysConfig();
            sysConfig.setConfigKey(entry.getKey());
            sysConfig.setConfigValue(entry.getValue());
            sysConfigRepository.save(sysConfig);
        }

        if(!sysConfigProperties.getSysProperty().getUploadMaxSize().equals(property.getUploadMaxSize())){

            // 修改上传配置
            try {
                // 1. 创建新的配置
                MultipartConfigFactory factory = new MultipartConfigFactory();
                factory.setMaxFileSize(DataSize.parse(property.getUploadMaxSize().toUpperCase()));
                factory.setMaxRequestSize(DataSize.parse(property.getUploadMaxSize().toUpperCase()));
                MultipartConfigElement newConfig = factory.createMultipartConfig();

                // 2. 获取 BeanFactory 并更新配置
                if (applicationContext instanceof ConfigurableApplicationContext) {
                    DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                            ((ConfigurableApplicationContext) applicationContext).getBeanFactory();

                    // 3. 更安全的销毁和重新注册流程
                    synchronized (this) { // 添加同步锁防止并发问题
                        // 先移除bean定义（如果存在）
                        if (beanFactory.containsBeanDefinition("multipartConfigElement")) {
                            beanFactory.removeBeanDefinition("multipartConfigElement");
                        }

                        // 销毁单例实例（如果存在）
                        if (beanFactory.containsSingleton("multipartConfigElement")) {
                            beanFactory.destroySingleton("multipartConfigElement");
                        }

                        // 注册新的单例
                        beanFactory.registerSingleton("multipartConfigElement", newConfig);

                        // 强制触发Servlet容器重新初始化
                        try {
                            ((ConfigurableApplicationContext) applicationContext).getBeanFactory().destroyScopedBean("multipartConfigElement");
                        } catch (Exception e) {
                            log.warn("Failed to destroy scoped bean", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to update multipart config", e);
                throw new RuntimeException("Failed to update upload size configuration", e);
            }
        }
        sysConfigProperties.setSysProperty(property);
    }
}