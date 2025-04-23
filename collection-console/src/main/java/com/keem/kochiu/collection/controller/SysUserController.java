package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.Add;
import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.annotation.Edit;
import com.keem.kochiu.collection.annotation.Module;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.*;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.UserVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

@RestController
@RequestMapping(PUBLIC_URL + "/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @CheckPermit
    @PostMapping("/list")
    public DefaultResult<PageVo<UserVo>> listUsers(SearchUserBo searchUserBo) throws CollectionException {

        return DefaultResult.ok(userService.listUsers(searchUserBo));
    }

    @CheckPermit(modules = {
        @Module(modeCode = "user", byAction = {"add"})
    })
    @PostMapping("/add")
    public DefaultResult<Boolean> addUser(@Validated({Add.class}) UserInfoBo userInfoBo) throws CollectionException {

        userService.addUser(userInfoBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "user", byAction = {"edit"})
    })
    @PostMapping("/update")
    public DefaultResult<Boolean> updateUser(@Validated({Edit.class}) UserInfoBo userInfoBo) throws CollectionException {

        userService.updateUser(userInfoBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "user", byAction = {"delete"})
    })
    @PostMapping("/delete")
    public DefaultResult<Boolean> deleteUser(RemoveUserBo removeUserBo) throws CollectionException {

        userService.deleteUser(removeUserBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "user", byAction = {"reset-pwd"})
    })
    @PostMapping("/resetpwd")
    public DefaultResult<Boolean> resetPwd(ResetPwdBo resetPwdBo) throws CollectionException {

        userService.resetPassword(resetPwdBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "user", byAction = {"enable-disable"})
    })
    @PostMapping("/enable-disable")
    public DefaultResult<Boolean> enableOrDisable(UserStatusBo userStatusBo) throws CollectionException {

        userService.enableOrDisable(userStatusBo);
        return DefaultResult.ok(true);
    }
}
