package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.service.FileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @CheckPermit
    @PostMapping("/api/v1/upload")
    public DefaultResult<FileVo> upload(@Valid UploadBo uploadBo) throws CollectionException {

        return DefaultResult.ok(fileService.saveFile(uploadBo));
    }

    @GetMapping("/{resourceId}/**")
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable int resourceId){

        fileService.download(request, response, resourceId);
    }
}
