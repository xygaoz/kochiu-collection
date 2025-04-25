package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginBo {

    private String nikeName;
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
}
