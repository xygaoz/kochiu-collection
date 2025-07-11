package com.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kochiu.collection.entity.UserResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserResourceMapper extends BaseMapper<UserResource> {

    Long selectLastInsertId();

    List<UserResource> selectTagResource(@Param("userId") int userId,
                                         @Param("tagId") int tagId,
                                         @Param("keyword") String keyword,
                                         @Param("types") Integer[] types
                                         );

    List<UserResource> selectCategoryResource(
            @Param("userId") int userId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectTypeResource(
            @Param("userId") int userId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectRecycleResource(
            @Param("userId") int userId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectAllCateResource(
            @Param("userId") int userId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectCatalogResourceIncludeSub(
            @Param("userId") int userId,
            @Param("cataId") Long cataId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectCatalogResource(
            @Param("userId") int userId,
            @Param("cataId") Long cataId,
            @Param("cateId") Long cateId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );

    List<UserResource> selectPublicResource(
            @Param("userId") int userId,
            @Param("keyword") String keyword,
            @Param("types") Integer[] types,
            @Param("tagNames") String[] tagNames
    );
}




