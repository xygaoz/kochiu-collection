package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.CategoryBo;
import com.keem.kochiu.collection.data.vo.CategoryVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserCategoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/category")
public class UserCategoryController {

    private final UserCategoryService userCategoryService;

    public UserCategoryController(UserCategoryService userCategoryService) {
        this.userCategoryService = userCategoryService;
    }

    @CheckPermit
    @GetMapping("/list")
    public DefaultResult<List<CategoryVo>> getCategoryList() throws CollectionException {

        return DefaultResult.ok(userCategoryService.getCategoryList(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @GetMapping("/all")
    public DefaultResult<List<CategoryVo>> getAllCategory() throws CollectionException {

        return DefaultResult.ok(userCategoryService.getAllCategory(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @PostMapping("/add")
    public DefaultResult<CategoryVo> addCategory(@Validated({Add.class}) CategoryBo categoryBo) throws CollectionException {

        return DefaultResult.ok(userCategoryService.addCategory(CheckPermitAspect.USER_INFO.get(), categoryBo));
    }
}
