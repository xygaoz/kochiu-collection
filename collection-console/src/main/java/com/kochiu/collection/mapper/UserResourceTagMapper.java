package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.UserResourceTag;

public interface UserResourceTagMapper extends BaseMapper<UserResourceTag> {

    Long selectLastInsertId();
}


