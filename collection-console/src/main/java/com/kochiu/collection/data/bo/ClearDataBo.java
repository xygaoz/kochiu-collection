package com.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClearDataBo {

    @NotBlank(message = "当前用户密码不能为空")
    private String password;
}
