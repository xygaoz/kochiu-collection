package com.keem.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keem.kochiu.collection.data.bo.CategoryBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.CategoryVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.RemoveEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCategoryRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.service.store.ResourceStoreStrategy;
import com.keem.kochiu.collection.service.store.ResourceStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.CATEGORY_IS_EXIST;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.CATEGORY_NOT_EXIST;

@Service
public class UserCategoryService {

    private final UserCategoryRepository userCategoryRepository;
    private final SysUserRepository userRepository;
    private final UserResourceRepository userResourceRepository;
    private final ResourceStrategyFactory resourceStrategyFactory;

    public UserCategoryService(UserCategoryRepository userCategoryRepository,
                               SysUserRepository userRepository,
                               UserResourceRepository userResourceRepository,
                               ResourceStrategyFactory resourceStrategyFactory) {
        this.userCategoryRepository = userCategoryRepository;
        this.userRepository = userRepository;
        this.userResourceRepository = userResourceRepository;
        this.resourceStrategyFactory = resourceStrategyFactory;
    }

    /**
     * 获取用户分类列表
     * @return
     */
    public List<CategoryVo> getCategoryList(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserCategory> categoryList = userCategoryRepository.getCategoryList(user.getUserId());
        return categoryList.stream().map(category -> CategoryVo.builder()
                .cateId(category.getCateId())
                .cateName(category.getCateName())
                .build()).toList();
    }

    public CategoryVo getCategoryInfo(UserDto userDto, long cateId) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        UserCategory category = userCategoryRepository.getOne(new LambdaQueryWrapper<UserCategory>()
                .eq(UserCategory::getUserId, user.getUserId())
                .eq(UserCategory::getCateId, cateId));
        return CategoryVo.builder()
                .cateId(category.getCateId())
                .cateName(category.getCateName())
                .build();
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

    /**
     * 修改分类
     * @param userDto
     * @param categoryBo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(UserDto userDto, CategoryBo categoryBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        userCategoryRepository.update(null,
                new LambdaUpdateWrapper<UserCategory>()
                        .set(UserCategory::getCateName, categoryBo.getCateName())
                        .eq(UserCategory::getUserId, user.getUserId())
                        .eq(UserCategory::getCateId, categoryBo.getCateId())
        );
    }


    /**
     * 删除分类
     * @param userDto
     * @param categoryBo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeCategory(UserDto userDto, CategoryBo categoryBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        //直接删除
        if(categoryBo.getRemoveType() == RemoveEnum.REMOVE_TYPE_DELETE) {
            //先取出分类下的资源文件克隆
            List<UserResource> resources = userResourceRepository.getResources(user.getUserId(), categoryBo.getCateId());
            List<UserResource> res = new ArrayList<>();
            for(UserResource resource : resources){
                res.add(resource.clone());
            }
            //删除资源
            userResourceRepository.removeByIds(resources, true);
            //删除分类
            userCategoryRepository.removeById(categoryBo.getCateId(), true);

            //物理删除文件
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            for(UserResource resource : res){
                resourceStoreStrategy.deleteFile(user.getUserId(), resource);
            }
        }
        else{
            //移动到分类
            if(userCategoryRepository.getById(categoryBo.getTargetCateId()) == null){
                throw new CollectionException(CATEGORY_NOT_EXIST);
            }

            //更新资源分类
            userResourceRepository.update(null,
                    new LambdaUpdateWrapper<UserResource>()
                            .set(UserResource::getCateId, categoryBo.getTargetCateId())
                            .eq(UserResource::getUserId, user.getUserId())
                            .eq(UserResource::getCateId, categoryBo.getCateId())
            );
            //删除分类
            userCategoryRepository.removeById(categoryBo.getCateId(), true);
        }
    }
}
