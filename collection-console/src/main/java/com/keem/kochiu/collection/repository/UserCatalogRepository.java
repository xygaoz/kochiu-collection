package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysConfig;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.mapper.SysConfigMapper;
import com.keem.kochiu.collection.mapper.UserCatalogMapper;
import org.springframework.stereotype.Service;

@Service
public class UserCatalogRepository extends ServiceImpl<UserCatalogMapper, UserCatalog>{

}
