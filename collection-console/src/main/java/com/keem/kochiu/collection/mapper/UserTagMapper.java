package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserTagMapper extends BaseMapper<UserTag> {

    Long selectLastInsertId();

    List<UserTag> listTagByResourceNum(@Param("userId") int userId,
                                       @Param("limit") int limit);

    List<UserTag> listTag(@Param("userId") int userId, @Param("resourceId") Long resourceId);
}


