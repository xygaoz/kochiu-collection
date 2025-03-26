package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.entity.UserResource;

public interface ResourceMapper extends BaseMapper<UserResource> {

    Long selectLastInsertId();
}




