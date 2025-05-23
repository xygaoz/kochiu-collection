package com.kochiu.collection.properties;

import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserConfig;
import com.kochiu.collection.enums.CategoryByEnum;
import com.kochiu.collection.enums.TagByEnum;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserConfigRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Data
public class UserConfigProperties {

    private final UserConfigRepository userConfigRepository;
    private final SysUserRepository sysUserRepository;
    private Map<Integer, UserProperty> userConfigMap;

    public UserConfigProperties(UserConfigRepository userConfigRepository,
                                SysUserRepository sysUserRepository) {
        this.userConfigRepository = userConfigRepository;
        this.sysUserRepository = sysUserRepository;
        userConfigMap = new HashMap<>();
    }

    public UserProperty getUserProperty(int userId) {
        if(userConfigMap != null && userConfigMap.containsKey(userId)){
            return userConfigMap.get(userId);
        }
        return new UserProperty();
    }

    public void afterPropertiesSet() throws Exception {
        List<SysUser> users = sysUserRepository.list();
        users.forEach(user -> {
            List<UserConfig> configList = userConfigRepository.getUserConfig(user.getUserId());
            if (configList == null || configList.isEmpty()) {
                return;
            }

            // 创建配置键到配置值的映射
            Map<String, String> configMap = configList.stream()
                    .collect(Collectors.toMap(
                            UserConfig::getConfigKey,
                            UserConfig::getConfigValue,
                            (existing, replacement) -> existing));

            // 创建新的UserProperty实例
            UserProperty userProperty = new UserProperty();

            // 获取UserProperty类的所有字段
            Field[] fields = UserProperty.class.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String configKey = camelToUnderline(field.getName());
                if (configMap.containsKey(configKey)) {
                    String configValue = configMap.get(configKey);
                    if(StringUtils.isBlank(configValue)){
                        continue;
                    }
                    try {
                        field.setAccessible(true);

                        if (field.getType() == int.class || field.getType() == Integer.class) {
                            field.setInt(userProperty, Integer.parseInt(configValue));
                        } else if (field.getType() == long.class || field.getType() == Long.class) {
                            field.setLong(userProperty, Long.parseLong(configValue));
                        } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            field.setBoolean(userProperty, Boolean.parseBoolean(configValue));
                        } else if (field.getType() == double.class || field.getType() == Double.class) {
                            field.setDouble(userProperty, Double.parseDouble(configValue));
                        } else if (field.getType() == float.class || field.getType() == Float.class) {
                            field.setFloat(userProperty, Float.parseFloat(configValue));
                        } else if (field.getType().isEnum()) {
                            try {
                                int code = Integer.parseInt(configValue);
                                Object enumValue = null;
                                if (field.getType() == CategoryByEnum.class) {
                                    enumValue = CategoryByEnum.getByCode(code);
                                } else if (field.getType() == TagByEnum.class) {
                                    enumValue = TagByEnum.getByCode(code);
                                }
                                if (enumValue != null) {
                                    field.set(userProperty, enumValue);
                                }
                            } catch (NumberFormatException e) {
                                try {
                                    Object enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), configValue);
                                    field.set(userProperty, enumValue);
                                } catch (IllegalArgumentException ex) {
                                    log.error("Invalid enum value: {}", configValue);
                                }
                            }
                        } else {
                            field.set(userProperty, configValue);
                        }
                    } catch (Exception e) {
                        log.error("Failed to set config value for {}: {}", field.getName(), e.getMessage());
                    }
                }
            }

            // 将配置好的UserProperty存入map
            userConfigMap.put(user.getUserId(), userProperty);
        });
    }

    /**
     * 将驼峰命名转换为下划线命名
     */
    private static String camelToUnderline(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public void setUserProperty(Integer userId, UserProperty userProperty) {
        userConfigMap.put(userId, userProperty);
    }

    @Data
    public static class UserProperty {

        // 首页展示的分类数量
        @Min(value = 1, message = "分类数量不能小于1")
        @Max(value = 20, message = "分类数量不能大于20")
        private int listCategoryNum = 5;
        // 首页展示的分类排序字段
        private CategoryByEnum listCategoryBy = CategoryByEnum.CREATE_TIME_ABS;
        // 分类资源分页大小
        @Min(value = 20, message = "资源分页不能小于20")
        @Max(value = 1000, message = "资源分页不能大于1000")
        private int resourcePageSize = 100;
        // 首页展示的标签数量
        @Min(value = 1, message = "标签数量不能小于1")
        @Max(value = 20, message = "标签数量不能大于20")
        private int listTagNum = 5;
        // 首页展示的标签排序字段
        private TagByEnum listTagBy = TagByEnum.CREATE_TIME_ABS;

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            Field[] fields = UserProperty.class.getDeclaredFields();

            for (Field field : fields) {
                // 跳过静态字段和userConfigRepository字段
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