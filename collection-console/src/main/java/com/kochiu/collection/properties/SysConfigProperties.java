package com.kochiu.collection.properties;

import com.kochiu.collection.entity.SysConfig;
import com.kochiu.collection.enums.CategoryByEnum;
import com.kochiu.collection.enums.TagByEnum;
import com.kochiu.collection.repository.SysConfigRepository;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class SysConfigProperties {

    // 首页展示的分类数量
    private int listCategoryNum = 5;
    // 首页展示的分类排序字段
    private CategoryByEnum listCategoryBy = CategoryByEnum.CREATE_TIME_ABS;
    // 分类资源分页大小
    private int categoryResourcePageSize = 100;
    // 首页展示的标签数量
    private int listTagNum = 5;
    // 首页展示的标签排序字段
    private TagByEnum listTagBy = TagByEnum.CREATE_TIME_ABS;
    // 标签资源分页大小
    private int tagResourcePageSize = 100;

    private final SysConfigRepository sysConfigRepository;

    public SysConfigProperties(SysConfigRepository sysConfigRepository) {
        this.sysConfigRepository = sysConfigRepository;
    }

    public void afterPropertiesSet() throws Exception {
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

        // 获取当前类的所有字段
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            // 跳过静态字段和sysConfigRepository字段
            if (Modifier.isStatic(field.getModifiers()) || "sysConfigRepository".equals(field.getName())) {
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
                        field.setInt(this, Integer.parseInt(configValue));
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        field.setLong(this, Long.parseLong(configValue));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.setBoolean(this, Boolean.parseBoolean(configValue));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.setDouble(this, Double.parseDouble(configValue));
                    } else if (field.getType() == float.class || field.getType() == Float.class) {
                        field.setFloat(this, Float.parseFloat(configValue));
                    } else if (field.getType().isEnum()) {
                        // 处理枚举类型
                        try {
                            int code = Integer.parseInt(configValue);
                            Object enumValue = null;
                            if (field.getType() == CategoryByEnum.class) {
                                enumValue = CategoryByEnum.getByCode(code);
                            } else if (field.getType() == TagByEnum.class) {
                                enumValue = TagByEnum.getByCode(code);
                            }
                            if (enumValue != null) {
                                field.set(this, enumValue);
                            } else {
                                System.err.println("Invalid code for CategoryByEnum: " + code);
                            }
                        } catch (NumberFormatException e) {
                            try {
                                Object enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), configValue);
                                field.set(this, enumValue);
                            } catch (IllegalArgumentException ex) {
                                System.err.println("Invalid value for CategoryByEnum: " + configValue);
                            }
                        }
                    } else {
                        // 其他类型直接设置为字符串
                        field.set(this, configValue);
                    }
                } catch (Exception e) {
                    // 转换失败时使用默认值，并记录日志
                    System.err.println("Failed to set config value for " + field.getName() +
                            ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * 将驼峰命名转换为下划线命名
     */
    private String camelToUnderline(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}