package com.kochiu.collection.annotation;

import com.kochiu.collection.enums.ResourceTypeEnum;

import java.lang.annotation.*;

import static com.kochiu.collection.enums.ResourceTypeEnum.UNKNOWN;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileType {

    boolean thumb() default false;
    boolean resolutionRatio() default false;
    String mimeType() default  "application/octet-stream";
    ResourceTypeEnum desc() default UNKNOWN;

}
