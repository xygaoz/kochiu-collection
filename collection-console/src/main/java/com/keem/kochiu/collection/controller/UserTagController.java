package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.dto.TagDto;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserResourceTagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

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

}
