package com.kochiu.collection.data.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EditMyNameBo {
    @NotNull(message = "用户名不能为空")
    private String userName;
}
