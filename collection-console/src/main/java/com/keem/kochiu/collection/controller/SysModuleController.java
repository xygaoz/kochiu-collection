package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.vo.ModuleVo;
import com.keem.kochiu.collection.service.CheckPermitAspect;
import com.keem.kochiu.collection.service.SysModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/module")
public class SysModuleController {

    private final SysModuleService sysModuleService;

    public SysModuleController(SysModuleService sysModuleService) {
        this.sysModuleService = sysModuleService;
    }

    @CheckPermit
    @GetMapping("/with-actions")
    public DefaultResult<Set<ModuleVo>> getPermitModuleList() {
        return DefaultResult.ok(sysModuleService.getPermitModuleList(CheckPermitAspect.USER_INFO.get()));
    }
}
