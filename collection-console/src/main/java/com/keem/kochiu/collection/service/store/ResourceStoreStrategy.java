package com.keem.kochiu.collection.service.store;

import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.exception.CollectionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ResourceStoreStrategy {

    /**
     * 保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    FileVo saveFile(UploadBo uploadBo, UserDto user) throws CollectionException;

    /**
     * 下载文件
     * @param request
     * @param response
     * @param resourceId
     */
    void download(HttpServletRequest request, HttpServletResponse response, int resourceId);

    /**
     * 删除文件
     * @param resourceId
     */
    void deleteFile(int resourceId);
}
