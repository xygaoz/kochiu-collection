package com.kochiu.collection.data.dto;

import com.kochiu.collection.annotation.Add;
import com.kochiu.collection.annotation.Remove;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Data
public class TagDto {

    @NotNull(groups = {Remove.class}, message = "tagId不能为空！！！")
    private Long tagId;
    @NotNull(groups = {Add.class}, message = "标签名称不能为空！！！")
    private String tagName;
    @NotNull(groups = {Add.class, Remove.class}, message = "资源Id不能为空！！！")
    private Long resourceId;
}
