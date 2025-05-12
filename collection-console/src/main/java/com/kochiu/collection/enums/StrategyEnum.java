package com.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum StrategyEnum {

    LOCAL( "local", "本地"),
    ;

    private final String code;
    private final String desc;
    StrategyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StrategyEnum getByCode(String code) {
        for (StrategyEnum strategyEnum : StrategyEnum.values()) {
            if (strategyEnum.getCode().equals(code)) {
                return strategyEnum;
            }
        }
        return null;
    }
}
