package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.dto.TagDto;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.UserResourceTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/tag")
public class UserTagController {

    private final UserResourceTagService tagService;

    public UserTagController(UserResourceTagService tagService) {
        this.tagService = tagService;
    }

    @CheckPermit
    @GetMapping("/list")
    public DefaultResult<List<TagDto>> getTagList() throws CollectionException {

        return DefaultResult.ok(tagService.getTagList(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @GetMapping("/all")
    public DefaultResult<List<TagDto>> getAllTag() throws CollectionException {

        return DefaultResult.ok(tagService.getAllTag(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @GetMapping("/get/{tagId}")
    public DefaultResult<TagDto> getTagInfo(@PathVariable Long tagId) throws CollectionException {

        return DefaultResult.ok(tagService.getTagInfo(CheckPermitAspect.USER_INFO.get(), tagId));
    }

}
