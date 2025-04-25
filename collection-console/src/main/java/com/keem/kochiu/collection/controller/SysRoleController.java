package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.annotation.Edit;
import com.keem.kochiu.collection.annotation.Module;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.RoleBo;
import com.keem.kochiu.collection.data.vo.RoleVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.SysRoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @CheckPermit(modules = {
            @Module(modeCode = "role", byAction = {"add"})
    })
    @PostMapping("/add")
    public DefaultResult<Boolean> addRole(@Validated({Add.class}) RoleBo roleBo) {
        sysRoleService.addRole(roleBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "role", byAction = {"edit"})
    })
    @PostMapping("/update")
    public DefaultResult<Boolean> updateRole(@Validated({Edit.class}) RoleBo roleBo) throws CollectionException {
        sysRoleService.updateRole(roleBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "role", byAction = {"delete"})
    })
    @GetMapping("/delete/{roleId}")
    public DefaultResult<Boolean> deleteRole(@PathVariable Long roleId) throws CollectionException {
        sysRoleService.deleteRole(roleId);
        return DefaultResult.ok(true);
    }
}
