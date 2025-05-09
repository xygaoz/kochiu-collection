package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.annotation.Edit;
import com.kochiu.collection.annotation.Remove;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.CatalogBo;
import com.kochiu.collection.data.vo.CatalogVo;
import com.kochiu.collection.data.vo.PathVo;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.UserCatalogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static com.kochiu.collection.Constant.PUBLIC_URL;

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
    @GetMapping("/path/{id}")
    public DefaultResult<PathVo> getCatalogPath(@PathVariable int id) throws CollectionException {
        return DefaultResult.ok(userCatalogService.getCatalogPath(CheckPermitAspect.USER_INFO.get(), id));
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

    @CheckPermit
    @PostMapping("/remove")
    public DefaultResult<Boolean> removeCatalog(@Validated({Remove.class}) CatalogBo catalogBo) throws CollectionException {
        userCatalogService.deleteCatalog(CheckPermitAspect.USER_INFO.get(), catalogBo);
        return DefaultResult.ok(true);
    }
}
