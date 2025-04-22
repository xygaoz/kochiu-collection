package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.SearchUserBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.ILLEGAL_REQUEST;

@Service
public class SysUserRepository extends ServiceImpl<SysUserMapper, SysUser>{

    public SysUser getUser(UserDto userDto) throws CollectionException {
        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        SysUser user = getById(userId);
        if (user == null) {
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        return user;
    }

    /**
     * 查询用户列表
     * @param searchUserBo
     * @return
     * @throws CollectionException
     */
    public PageInfo<SysUser> listUser(SearchUserBo searchUserBo) throws CollectionException {

        try(Page<SysUser> page = PageHelper.startPage(searchUserBo.getPageNum(), searchUserBo.getPageSize())) {

            LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(searchUserBo.getUserCode() != null){
                lambdaQueryWrapper.like(SysUser::getUserCode, searchUserBo.getUserCode());
            }
            if(searchUserBo.getUserName() != null){
                lambdaQueryWrapper.like(SysUser::getUserName, searchUserBo.getUserName());
            }
            return new PageInfo<>(list(lambdaQueryWrapper));
        }
    }
}
