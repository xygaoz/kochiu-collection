package com.keem.kochiu.collection.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Data
public class TagDto {

    @NotNull(message = "tagId不能为空")
    private Long tagId;
    private String tagName;
}
