package com.kochiu.collection.data.bo;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class LoginBo {

    private String nikeName;
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
    @NotNull(message = "设备指纹不能为空")
    private String deviceFingerprint;
    private String ip;
}
