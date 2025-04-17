package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.vo.CatalogVo;
import com.keem.kochiu.collection.data.vo.PathVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/catalog")
public class UserCatalogController {

    private final UserCatalogService userCatalogService;
    public UserCatalogController(UserCatalogService userCatalogService) {
        this.userCatalogService = userCatalogService;
    }

    @CheckPermit
    @GetMapping("/tree")
    public DefaultResult<List<CatalogVo>> getCatalogTree() throws CollectionException {

        return DefaultResult.ok(Collections.singletonList(userCatalogService.getCatalogTree(CheckPermitAspect.USER_INFO.get())));
    }

    @CheckPermit
    @GetMapping("/path/{sno}")
    public DefaultResult<PathVo> getCatalogPath(@PathVariable int sno) throws CollectionException {
        return DefaultResult.ok(userCatalogService.getCatalogPath(CheckPermitAspect.USER_INFO.get(), sno));
    }
}
