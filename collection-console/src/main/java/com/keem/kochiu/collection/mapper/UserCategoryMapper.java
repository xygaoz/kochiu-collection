package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCategoryMapper extends BaseMapper<UserCategory> {

    Long selectLastInsertId();

    List<UserCategory> listCategoryByResourceNum(@Param("userId") int userId,
                                                 @Param("limit") int limit);
}




