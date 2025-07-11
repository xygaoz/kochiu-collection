package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kochiu.collection.data.bo.BatchTagBo;
import com.kochiu.collection.data.dto.TagDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserTag;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserResourceTagRepository;
import com.kochiu.collection.repository.UserTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.kochiu.collection.enums.ErrorCodeEnum.TAG_IS_EXIST;

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
    @Transactional(rollbackFor = Exception.class)
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
            throw new CollectionException(TAG_IS_EXIST);
        }
    }

    /**
     * 批量添加资源标签
     * @param userDto
     * @param batchTagBo
     * @return
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void removeResourceTag(UserDto userDto, TagDto tagDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        userResourceTagRepository.removeResourceTag(user.getUserId(), tagDto.getResourceId(), tagDto.getTagId());
    }

    /**
     * 批量删除资源标签
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 获取所有标签
     * @param userDto
     * @return
     * @throws CollectionException
     */
    public List<TagDto> getAllTag(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserTag> tagList = userTagRepository.getTagList(user.getUserId(), null);

        return tagList.stream().map(tag -> TagDto.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build()).toList();
   }

    /**
     * 获取标签信息
     * @param userDto
     * @param tagId
     * @return
     * @throws CollectionException
     */
   public TagDto getTagInfo(UserDto userDto, Long tagId) throws CollectionException {

       SysUser user = userRepository.getUser(userDto);
       UserTag userTag = userTagRepository.getOne(Wrappers.lambdaQuery(UserTag.class)
               .eq(UserTag::getUserId, user.getUserId())
               .eq(UserTag::getTagId, tagId));

       return TagDto.builder()
               .tagId(userTag.getTagId())
               .tagName(userTag.getTagName())
               .build();
    }
}
