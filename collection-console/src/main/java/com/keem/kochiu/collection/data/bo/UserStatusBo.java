package com.keem.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserStatusBo {
    @NotNull(message = "用户Id不能为空")
    private Integer userId;
    @NotNull(message = "用户状态不能为空")
    private String status;
}
