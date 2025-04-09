package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.bo.ResInfoBo;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserTag;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserResourceTagRepository;
import com.keem.kochiu.collection.repository.UserTagRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public TagDto addResourceTag(UserDto userDto, ResInfoBo resourceInfo) throws CollectionException {

        if(StringUtils.isBlank(resourceInfo.getTagName())){
            throw new CollectionException("标签不能为空");
        }
        SysUser user = userRepository.getUser(userDto);
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
