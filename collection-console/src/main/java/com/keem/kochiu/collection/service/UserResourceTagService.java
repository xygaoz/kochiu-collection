package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.bo.BatchTagBo;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserTag;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserResourceTagRepository;
import com.keem.kochiu.collection.repository.UserTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserResourceTagService {

    private final UserResourceTagRepository userResourceTagRepository;
    private final SysUserRepository userRepository;
    private final UserTagRepository userTagRepository;

    public UserResourceTagService(UserResourceTagRepository userResourceTagRepository,
                                  SysUserRepository userRepository,
                                  UserTagRepository userTagRepository) {
        this.userResourceTagRepository = userResourceTagRepository;
        this.userRepository = userRepository;
        this.userTagRepository = userTagRepository;
    }


    /**
     * 添加资源标签
     * @param userDto
     * @param resourceInfo
     * @return
     * @throws CollectionException
     */
    public TagDto addResourceTag(UserDto userDto, TagDto resourceInfo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        return addResourceTag(user, resourceInfo);
    }

    /**
     * 公共添加资源标签
     * @param user
     * @param resourceInfo
     * @return
     * @throws CollectionException
     */
    private TagDto addResourceTag(SysUser user, TagDto resourceInfo) throws CollectionException {

        Long tagId = userTagRepository.existsTag(user.getUserId(), resourceInfo.getTagName());
        if(tagId == null){
            tagId = userTagRepository.addTag(user.getUserId(), resourceInfo.getTagName());
        }

        if(!userResourceTagRepository.existsTag(user.getUserId(), resourceInfo.getResourceId(), tagId)) {
            userResourceTagRepository.addTag(user.getUserId(), resourceInfo.getResourceId(), tagId);
            return TagDto.builder()
                    .tagId(tagId)
                    .tagName(resourceInfo.getTagName())
                    .build();
        }
        else{
            throw new CollectionException("标签已存在");
        }
    }

    /**
     * 批量添加资源标签
     * @param userDto
     * @param batchTagBo
     * @return
     * @throws CollectionException
     */
    public TagDto batchAddTag(UserDto userDto, BatchTagBo batchTagBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        AtomicReference<TagDto> returnTagDto = new AtomicReference<>();
        batchTagBo.getResourceIds().forEach(resourceId -> {
            try {
                returnTagDto.set(addResourceTag(user, TagDto.builder()
                        .resourceId(resourceId)
                        .tagName(batchTagBo.getTagName())
                        .build()));
            } catch (CollectionException ignored) {
                // ignored
            }
        });
        return returnTagDto.get();
    }

    /**
     * 删除资源标签
     * @param userDto
     * @param tagDto
     * @throws CollectionException
     */
    public void removeResourceTag(UserDto userDto, TagDto tagDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        userResourceTagRepository.removeResourceTag(user.getUserId(), tagDto.getResourceId(), tagDto.getTagId());
    }

    /**
     * 批量删除资源标签
     */
    public void batchRemoveTag(UserDto userDto, BatchTagBo batchTagBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        batchTagBo.getResourceIds().forEach(resourceId -> {
            try {
                userResourceTagRepository.removeResourceTag(user.getUserId(), resourceId, batchTagBo.getTagId());
            } catch (CollectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 获取资源标签列表(菜单专用)
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public List<TagDto> getTagList(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserTag> tagList = userTagRepository.getTagList(user.getUserId());

        return tagList.stream().map(tag -> TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build()).toList();
    }
}
