package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserResourceTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserResourceTagMapper extends BaseMapper<UserResourceTag> {

    Long selectLastInsertId();
}


