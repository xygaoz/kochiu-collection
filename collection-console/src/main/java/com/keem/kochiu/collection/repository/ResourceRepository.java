package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
     * @param userId
     * @param cateId
     * @param sourceFileName
     * @param resourceUrl
     * @param fileExt
     * @param resolutionRatio
     * @param size
     * @param thumbUrl
     * @return
     * @throws CollectionException
     */
    public Long saveResource(int userId, int cateId,
                             String sourceFileName, String resourceUrl,
                             String fileExt, String resolutionRatio,
                             Long size, String thumbUrl) throws CollectionException {

        UserResource userResource = new UserResource();
        userResource.setUserId(userId);
        userResource.setCateId(categoryRepository.getCateId(userId, cateId));
        userResource.setSourceFileName(sourceFileName);
        userResource.setResourceUrl(resourceUrl);
        userResource.setFileExt(fileExt);
        userResource.setResolutionRatio(resolutionRatio);
        userResource.setSize(size);
        userResource.setThumbUrl(thumbUrl);
        if (this.save(userResource)) {
            // 获取最后插入的行ID
            Long resourceId = baseMapper.selectLastInsertId();
            userResource.setResourceId(resourceId);
            return resourceId;
        }

        return 0L;
    }
}
