package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserResourceMapper extends BaseMapper<UserResource> {

    Long selectLastInsertId();

    List<UserResource> selectTagResource(@Param("userId") int userId, @Param("tagId") int tagId);
}




