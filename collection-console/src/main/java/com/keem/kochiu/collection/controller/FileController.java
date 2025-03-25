package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    @CheckPermit
    @PostMapping("/upload")
    public DefaultResult<FileVo> upload(@Valid UploadBo uploadBo) {
        return DefaultResult.ok();
    }
}
