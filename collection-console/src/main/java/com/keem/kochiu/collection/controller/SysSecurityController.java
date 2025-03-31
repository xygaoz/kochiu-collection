package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.LoginBo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.SysSecurityService;
import com.keem.kochiu.collection.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class SysSecurityController {

    private final SysUserService userService;
    private final SysSecurityService securityService;

    public SysSecurityController(SysUserService userService, SysSecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
    }

    /**
     * 生成api token
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    @PostMapping("/tokens")
    public DefaultResult<String> tokens(@Valid LoginBo loginBo) throws CollectionException {

        return DefaultResult.ok(userService.genToken(loginBo));
    }

    /**
     * 获取公钥
     * @return
     * @throws CollectionException
     */
    @GetMapping("/publicKey")
    public DefaultResult<String> getPublicKey() throws CollectionException {
        return DefaultResult.ok(securityService.getPublicKey());
    }
}
