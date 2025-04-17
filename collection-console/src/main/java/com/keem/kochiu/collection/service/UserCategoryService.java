package com.keem.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.CategoryBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.CategoryVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.CATEGORY_IS_EXIST;

@Service
public class UserCategoryService {

    private final UserCategoryRepository userCategoryRepository;
    private final SysUserRepository userRepository;

    public UserCategoryService(UserCategoryRepository userCategoryRepository,
                               SysUserRepository userRepository) {
        this.userCategoryRepository = userCategoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取用户分类列表
     * @return
     */
    public List<CategoryVo> getCategoryList(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserCategory> categoryList = userCategoryRepository.getCategoryList(user.getUserId());
        return categoryList.stream().map(category -> CategoryVo.builder()
                .cateName(category.getCateName())
                .sno(category.getSno())
                .build()).toList();
    }

    /**
     * 获取所有分类列表
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public List<CategoryVo> getAllCategory(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserCategory> categoryList = userCategoryRepository.getAllCategory(user.getUserId());
        return categoryList.stream().map(category -> CategoryVo.builder()
                .cateId(category.getCateId())
                .cateName(category.getCateName())
                .sno(category.getSno())
                .build()).toList();
    }

    /**
     * 添加分类
     * @param userDto
     * @param categoryBo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryVo addCategory(UserDto userDto, CategoryBo categoryBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        LambdaQueryWrapper<UserCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCategory::getUserId, user.getUserId())
                .eq(UserCategory::getCateName, categoryBo.getCateName());
        if(userCategoryRepository.exists(queryWrapper)){
            throw new CollectionException(CATEGORY_IS_EXIST);
        }
        return userCategoryRepository.addCategory(user.getUserId(), categoryBo.getCateName());
    }
}
