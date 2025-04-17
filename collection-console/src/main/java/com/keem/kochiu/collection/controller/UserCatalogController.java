package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.annotation.Edit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.CatalogBo;
import com.keem.kochiu.collection.data.vo.CatalogVo;
import com.keem.kochiu.collection.data.vo.PathVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.UserCatalogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @CheckPermit
    @PostMapping("/add")
    public DefaultResult<Boolean> addCatalog(@Validated({Add.class}) CatalogBo catalogBo) throws CollectionException {
        userCatalogService.addCatalog(CheckPermitAspect.USER_INFO.get(), catalogBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping("/update")
    public DefaultResult<Boolean> updateCatalog(@Validated({Edit.class}) CatalogBo catalogBo) throws CollectionException {
        userCatalogService.updateCatalog(CheckPermitAspect.USER_INFO.get(), catalogBo);
        return DefaultResult.ok(true);
    }
}
