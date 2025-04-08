package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class SysUserRepository extends ServiceImpl<SysUserMapper, SysUser>{

    public SysUser getUser(UserDto userDto) throws CollectionException {
        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        SysUser user = getById(userId);
        if (user == null) {
            throw new CollectionException("非法请求。");
        }

        return user;
    }
}
