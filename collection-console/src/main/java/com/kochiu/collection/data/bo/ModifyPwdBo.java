package com.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ModifyPwdBo {

    @NotNull(message = "旧密码不能为空")
    private String oldPassword;
    @NotNull(message = "新密码不能为空")
    private String newPassword;
}
