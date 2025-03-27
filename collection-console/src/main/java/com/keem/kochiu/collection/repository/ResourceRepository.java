package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.entity.UserCategory;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.CategoryMapper;
import com.keem.kochiu.collection.mapper.ResourceMapper;
import org.springframework.stereotype.Service;

@Service
public class ResourceRepository extends ServiceImpl<ResourceMapper, UserResource>{

    private final CategoryRepository categoryRepository;

    public ResourceRepository(CategoryRepository categoryRepository) {
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
