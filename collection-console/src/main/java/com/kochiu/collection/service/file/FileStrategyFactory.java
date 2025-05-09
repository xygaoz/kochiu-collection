package com.kochiu.collection.service.file;

import com.kochiu.collection.enums.FileTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.kochiu.collection.enums.FileTypeEnum.unknown;

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
     * @param type 策略标识
     * @return 具体策略实例
     */
    public FileStrategy getStrategy(FileTypeEnum type) {
        FileStrategy strategy = strategyMap.get(type.name());
        if (strategy == null) {
            return strategyMap.get(unknown.name());
        }
        return strategy;
    }
}
