package com.keem.kochiu.collection.data.bo;

import com.keem.kochiu.collection.annotation.Edit;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RoleBo {

    @NotNull(groups = {Edit.class}, message = "角色ID不能为空")
    private Integer roleId;
    @NotNull(message = "角色名称不能为空")
    private String roleName;
    private Long[] permissions;
}
