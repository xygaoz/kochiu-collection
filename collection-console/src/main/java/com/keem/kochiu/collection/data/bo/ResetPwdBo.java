package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetPwdBo {

    @NotNull(message = "用户Id不能为空")
    private Integer userId;
    @NotNull(message = "新密码不能为空")
    private String password;
    @NotNull(message = "确认密码不能为空")
    private String confirmPassword;
}
