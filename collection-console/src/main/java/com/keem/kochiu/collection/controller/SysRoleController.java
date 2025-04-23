package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.vo.RoleVo;
import com.keem.kochiu.collection.service.SysRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/role")
public class SysRoleController {

    private SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @CheckPermit
    @GetMapping("/list")
    public DefaultResult<List<RoleVo>> getRoleList() {
        return DefaultResult.ok(sysRoleService.selectAll());
    }
}
