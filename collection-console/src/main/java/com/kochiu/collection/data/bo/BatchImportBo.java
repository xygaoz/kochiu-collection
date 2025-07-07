package com.kochiu.collection.data.bo;

import com.kochiu.collection.enums.AutoCreateRuleEnum;
import com.kochiu.collection.enums.ImportMethodEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BatchImportBo {

    @NotNull(message = "服务端资源路径不能为空！！！")
    private String sourcePath;
    @NotNull(message = "资源分类ID不能为空！！！")
    private Long categoryId;
    private Long catalogId;
    @NotNull(message = "自动创建规则不能为空！！！")
    private AutoCreateRuleEnum autoCreateRule;
    @NotNull(message = "导入方式不能为空！！！")
    private ImportMethodEnum importMethod;
    private boolean includeSubDir = true;
    private String excludePattern;
    private String excludeFileExt;
    private int excludeFileSize;
}
