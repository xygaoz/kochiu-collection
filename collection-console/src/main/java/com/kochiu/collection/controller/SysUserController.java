package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.annotation.Edit;
import com.kochiu.collection.annotation.Module;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.*;
import com.kochiu.collection.data.vo.MenuVo;
import com.kochiu.collection.data.vo.PageVo;
import com.kochiu.collection.data.vo.UserVo;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.kochiu.collection.Constant.PUBLIC_URL;

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

        userService.resetPassword(CheckPermitAspect.USER_INFO.get(), resetPwdBo);
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

    @CheckPermit
    @GetMapping("/my-menu")
    public DefaultResult<Set<MenuVo>> getMyMenu() throws CollectionException {

        return DefaultResult.ok(userService.getMyMenu(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @GetMapping("/my-info")
    public DefaultResult<UserVo> getMyInfo() throws CollectionException {

        return DefaultResult.ok(userService.getMyInfo(CheckPermitAspect.USER_INFO.get()));
    }

    @CheckPermit
    @PostMapping("/set-my-name")
    public DefaultResult<Boolean> setMyName(@Validated EditMyNameBo editMyNameBo) throws CollectionException {

        userService.updateUser(CheckPermitAspect.USER_INFO.get(), editMyNameBo);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @GetMapping("/reset-key")
    public DefaultResult<Boolean> resetKey() throws CollectionException {

        userService.resetKey(CheckPermitAspect.USER_INFO.get());
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @GetMapping("/reset-token")
    public DefaultResult<Boolean> resetToken() throws CollectionException {

        userService.resetToken(CheckPermitAspect.USER_INFO.get());
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @PostMapping("/modify-pwd")
    public DefaultResult<Boolean> modifyPwd(@Validated ModifyPwdBo modifyPwdBo) throws CollectionException {

        userService.modifyPassword(CheckPermitAspect.USER_INFO.get(), modifyPwdBo);
        return DefaultResult.ok(true);
    }
}
