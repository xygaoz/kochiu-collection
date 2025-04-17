package com.keem.kochiu.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keem.kochiu.collection.entity.UserCatalog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCatalogMapper extends BaseMapper<UserCatalog> {

    Long selectLastInsertId();

    List<UserCatalog> selectParentCata(
            @Param("userId") int userId,
            @Param("cataId") long cataId
    );
}
