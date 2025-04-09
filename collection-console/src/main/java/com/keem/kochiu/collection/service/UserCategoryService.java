package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.CategoryVo;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.UserCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCategoryService {

    private final UserCategoryRepository userCategoryRepository;

    public UserCategoryService(UserCategoryRepository userCategoryRepository) {
        this.userCategoryRepository = userCategoryRepository;
    }

    /**
     * 获取用户分类列表
     * @return
     */
    public List<CategoryVo> getCategoryList(UserDto userDto) throws CollectionException {

        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        List<UserCategory> categoryList = userCategoryRepository.getCategoryList(userId);
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

        Integer userId = userDto != null ? userDto.getUserId() : null;
        if(userId == null){
            throw new CollectionException("非法请求。");
        }

        List<UserCategory> categoryList = userCategoryRepository.getAllCategory(userId);
        return categoryList.stream().map(category -> CategoryVo.builder()
                .cateName(category.getCateName())
                .sno(category.getSno())
                .build()).toList();
    }
}
