package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.vo.ModuleVo;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.SysModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.kochiu.collection.Constant.PUBLIC_URL;

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
