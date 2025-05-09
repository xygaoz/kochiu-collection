package com.kochiu.collection.data.bo;

import com.kochiu.collection.enums.RemoveUserOptionEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RemoveUserBo {

    @NotNull(message = "用户Id不能为空")
    private Integer userId;
    @NotNull(message = "删除选项不能为空")
    private RemoveUserOptionEnum deleteOption;
}
