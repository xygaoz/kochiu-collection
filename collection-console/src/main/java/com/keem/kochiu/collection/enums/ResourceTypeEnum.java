package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum ResourceTypeEnum {

    IMAGE(1, "图像"),
    VIDEO(2, "视频"),
    AUDIO(3, "音频"),
    DOCUMENT(4, "文档"),
    OTHER(5, "其他"),
    UNKNOWN(0, "未知"),
    ;

    final int i;
    final String label;

    ResourceTypeEnum(int i, String label) {
        this.i = i;
        this.label = label;
    }

    public static ResourceTypeEnum getByValue(int i) {
        for (ResourceTypeEnum resourceTypeEnum : values()) {
            if (resourceTypeEnum.i == i) {
                return resourceTypeEnum;
            }
        }
        return null;
    }

    public static ResourceTypeEnum getByValue(String name) {
        for (ResourceTypeEnum resourceTypeEnum : values()) {
            if (resourceTypeEnum.name().equalsIgnoreCase(name)) {
                return resourceTypeEnum;
            }
        }
        return null;
    }

}
