package com.kochiu.collection.properties;

import com.kochiu.collection.entity.SysConfig;
import com.kochiu.collection.repository.SysConfigRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Data
public class SysConfigProperties {

    private final SysConfigRepository sysConfigRepository;
    private SysProperty sysProperty = new SysProperty();
    private String maxFileSize;

    public SysConfigProperties(SysConfigRepository sysConfigRepository, String maxFileSize) {
        this.sysConfigRepository = sysConfigRepository;
        this.maxFileSize = maxFileSize;
    }

    public void afterPropertiesSet() {
        List<SysConfig> configList = sysConfigRepository.list();
        if (configList == null || configList.isEmpty()) {
            return;
        }

        // 创建配置键到配置值的映射
        Map<String, String> configMap = configList.stream()
                .collect(Collectors.toMap(
                        SysConfig::getConfigKey,
                        SysConfig::getConfigValue,
                        (existing, replacement) -> existing)); // 如果有重复键，保留现有值

        SysProperty sysProperty = new SysProperty();

        // 获取当前类的所有字段
        Field[] fields = SysProperty.class.getDeclaredFields();

        for (Field field : fields) {
            // 跳过静态字段和sysConfigRepository字段
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            // 构建配置键，通常是将驼峰命名转为下划线命名
            String configKey = camelToUnderline(field.getName());

            // 检查配置映射中是否有对应的值
            if (configMap.containsKey(configKey)) {
                String configValue = configMap.get(configKey);
                try {
                    // 设置字段可访问
                    field.setAccessible(true);

                    // 根据字段类型转换配置值
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.setInt(sysProperty, Integer.parseInt(configValue));
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        field.setLong(sysProperty, Long.parseLong(configValue));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.setBoolean(sysProperty, Boolean.parseBoolean(configValue));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.setDouble(sysProperty, Double.parseDouble(configValue));
                    } else if (field.getType() == float.class || field.getType() == Float.class) {
                        field.setFloat(sysProperty, Float.parseFloat(configValue));
                    } else {
                        // 其他类型直接设置为字符串
                        field.set(sysProperty, configValue);
                    }
                } catch (Exception e) {
                    // 转换失败时使用默认值，并记录日志
                    System.err.println("Failed to set config value for " + field.getName() +
                            ": " + e.getMessage());
                }
            }
        }
        sysProperty.setUploadMaxSize(maxFileSize);
        this.sysProperty = sysProperty;
    }

    /**
     * 将驼峰命名转换为下划线命名
     */
    private static String camelToUnderline(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Data
    public static class SysProperty {
        // 上传文件大小限制
        @NotNull
        @Pattern(regexp = "^\\d+(\\.\\d+)?\\s*(MB|GB|TB)$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "必须是以MB/GB/TB结尾的大小格式")
        private String uploadMaxSize = "2GB";

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            Field[] fields = SysProperty.class.getDeclaredFields();

            for (Field field : fields) {
                // 跳过静态字段
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                // 构建配置键，通常是将驼峰命名转为下划线命名
                String configKey = camelToUnderline(field.getName());
                String configValue = null;
                try {
                    // 设置字段可访问
                    field.setAccessible(true);
                    configValue = field.get(this).toString();
                }
                catch (Exception e){
                    log.error("设置字段可访问失败", e);
                }

                if(StringUtils.isNotBlank(configValue)) {
                    map.put(configKey, configValue);
                }
            }
            return map;
        }
    }
}