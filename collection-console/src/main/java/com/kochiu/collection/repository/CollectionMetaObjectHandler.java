package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kochiu.collection.service.CheckPermitAspect;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CollectionMetaObjectHandler implements MetaObjectHandler {

    // 插入时触发
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
    }

    // 更新时触发
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    public String getCurrentUser() {
        return CheckPermitAspect.USER_INFO.get() != null ? CheckPermitAspect.USER_INFO.get().getUserCode() : null;
    }
}