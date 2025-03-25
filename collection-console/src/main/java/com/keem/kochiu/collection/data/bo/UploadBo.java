package com.keem.kochiu.collection.data.bo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class UploadBo {

    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    private int categoryId;
}
