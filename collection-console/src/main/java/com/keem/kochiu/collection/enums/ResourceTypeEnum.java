package com.keem.kochiu.collection.enums;

public enum ResourceTypeEnum {

    UNKNOWN(0),
    IMAGE(1),
    VIDEO(2),
    AUDIO(3),
    DOCUMENT(4),
    OTHER(5),
    ;

    final int i;

    ResourceTypeEnum(int i) {
        this.i = i;
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

    public int getValue() {
        return i;
    }
}
