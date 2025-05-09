package com.kochiu.collection.annotation;

/**
 *
 * @author KoChiu
 * @date 2022/6/22
 */
public @interface Module {

    String modeCode();
     /*
     根据模块动作编码来授权，即当前用户如果有某个模块的某个动作权限则通过。默认不检查，动作可以多个
      */
    String[] byAction() default {};
}
