package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class FileStrategyFactory {

    // 自动注入所有策略实现（Key为Bean名称，Value为策略实例）
    private final Map<String, FileStrategy> strategyMap;

    @Autowired
    public FileStrategyFactory(Map<String, FileStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * 根据存储类型获取策略
     * @param extension 扩展名
     * @return 具体策略实例
     */
    public FileStrategy getStrategy(String extension) {

        for(String key : strategyMap.keySet()){
            if(key.equalsIgnoreCase(extension)){
                return strategyMap.get(key);
            }
        }
        return strategyMap.get("unknown");
    }

    public FileType getFileType(String extension) {
        FileStrategy fileStrategy = getStrategy(extension);
        return fileStrategy.getClass().getAnnotation(FileType.class);
    }

    public FileStrategy getStrategy(FileType fileType) {
        for(String key : strategyMap.keySet()){
            if(strategyMap.get(key).getClass().getAnnotation(FileType.class).equals(fileType)){
                return strategyMap.get(key);
            }
        }
        return strategyMap.get("unknown");
    }

    //  获取所有允许的mimeType
    public Set<String> getAllowedTypes() {

        Set<String> types = new HashSet<>();
        strategyMap.forEach((k, v) -> {
            types.add(v.getClass().getAnnotation(FileType.class).mimeType());
        });
        return types;
    }
}
