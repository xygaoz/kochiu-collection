package com.kochiu.collection.data.bo;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.Edit;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class UserInfoBo {

    @NotNull(groups = {Edit.class}, message = "用户Id不能为空")
    private Integer userId;
    @NotNull(message = "用户编码不能为空")
    private String userCode;
    @NotNull(message = "用户名称不能为空")
    private String userName;
    @NotNull(groups = {Add.class}, message = "登录密码不能为空")
    private String password;
    @NotNull(message = "存储不能为空")
    private String strategy;
    @NotNull(message = "角色不能为空")
    @Length(min = 1, message = "角色不能为空")
    private Integer[] roles;
}
