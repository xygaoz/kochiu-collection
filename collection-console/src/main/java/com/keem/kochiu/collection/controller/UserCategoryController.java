package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.vo.CategoryVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL)
public class UserCategoryController {

    private final UserCategoryService userCategoryService;

    public UserCategoryController(UserCategoryService userCategoryService) {
        this.userCategoryService = userCategoryService;
    }

    @CheckPermit
    @GetMapping("/category/list")
    public DefaultResult<List<CategoryVo>> getCategoryList() throws CollectionException {

        return DefaultResult.ok(userCategoryService.getCategoryList(CheckPermitAspect.USER_INFO.get()));
    }
}
