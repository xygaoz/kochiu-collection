package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.UserResourceMapper;
import org.springframework.stereotype.Service;

@Service
public class UserResourceRepository extends ServiceImpl<UserResourceMapper, UserResource>{

    private final UserCategoryRepository categoryRepository;

    public UserResourceRepository(UserCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * 保存资源，返回资源id
     * @param resourceDto
     * @return
     * @throws CollectionException
     */
    public Long saveResource(ResourceDto resourceDto) throws CollectionException {

        UserResource userResource = new UserResource();
        userResource.setUserId(resourceDto.getUserId());
        userResource.setCateId(categoryRepository.getCateId(resourceDto.getUserId(), resourceDto.getCateId()));
        userResource.setSourceFileName(resourceDto.getSourceFileName());
        userResource.setResourceUrl(resourceDto.getResourceUrl());
        userResource.setFileExt(resourceDto.getFileExt());
        userResource.setResolutionRatio(resourceDto.getResolutionRatio());
        userResource.setSize(resourceDto.getSize());
        userResource.setThumbUrl(resourceDto.getThumbUrl());
        if (this.save(userResource)) {
            // 获取最后插入的行ID
            Long resourceId = baseMapper.selectLastInsertId();
            userResource.setResourceId(resourceId);
            return resourceId;
        }

        return 0L;
    }
}
