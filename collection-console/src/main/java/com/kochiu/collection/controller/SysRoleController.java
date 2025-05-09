package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.annotation.Edit;
import com.kochiu.collection.annotation.Module;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.RoleBo;
import com.kochiu.collection.data.vo.RoleVo;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.service.SysRoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.kochiu.collection.Constant.PUBLIC_URL;

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
