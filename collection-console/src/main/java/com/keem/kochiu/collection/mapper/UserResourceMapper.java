package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserResourceMapper extends BaseMapper<UserResource> {

    Long selectLastInsertId();

    List<UserResource> selectTagResource(@Param("userId") int userId,
                                         @Param("tagId") int tagId,
                                         @Param("keyword") String keyword,
                                         @Param("types") String[] types
                                         );

    List<UserResource> selectCategoryResource(
            @Param("userId") int userId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") String[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectTypeResource(
            @Param("userId") int userId,
            @Param("keyword") String keyword,
            @Param("types") String[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectRecycleResource(
            @Param("userId") int userId,
            @Param("keyword") String keyword,
            @Param("types") String[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectAllResource(
            @Param("userId") int userId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") String[] types,
            @Param("tagNames") String[] tagNames
    );
}




