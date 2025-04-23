package com.keem.kochiu.collection.annotation;

import com.keem.kochiu.collection.enums.PermitEnum;

import java.lang.annotation.*;

/**
 * 检查权限
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPermit {

    PermitEnum[] on() default PermitEnum.UI;
    Module[] modules() default {};
}
