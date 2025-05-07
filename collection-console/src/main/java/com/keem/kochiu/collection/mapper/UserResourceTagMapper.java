package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserResourceTag;

public interface UserResourceTagMapper extends BaseMapper<UserResourceTag> {

    Long selectLastInsertId();
}


