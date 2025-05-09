package com.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.mapper.UserCatalogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCatalogRepository extends ServiceImpl<UserCatalogMapper, UserCatalog>{

    public List<UserCatalog> selectParentCata(int userId, long cataId){
        return baseMapper.selectParentCata(userId, cataId);
    }

    public Long insert(UserCatalog userCatalog){
        baseMapper.insert(userCatalog);
        return baseMapper.selectLastInsertId();
    }

    public Long getUserRoot(int userId){
        try {
            return getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, userId)
                    .eq(UserCatalog::getCataLevel, 0)
            ).getCataId();
        }
        catch (Exception e){
            return null;
        }
    }
}
