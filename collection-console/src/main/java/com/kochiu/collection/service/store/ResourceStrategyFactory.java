package com.kochiu.collection.service.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ResourceStrategyFactory {

    // 自动注入所有策略实现（Key为Bean名称，Value为策略实例）
    private final Map<String, ResourceStoreStrategy> strategyMap;

    @Autowired
    public ResourceStrategyFactory(Map<String, ResourceStoreStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * 根据存储类型获取策略
     * @param type 策略标识
     * @return 具体策略实例
     */
    public ResourceStoreStrategy getStrategy(String type) {
        ResourceStoreStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            return strategyMap.get("local");
        }
        return strategy;
    }
}
