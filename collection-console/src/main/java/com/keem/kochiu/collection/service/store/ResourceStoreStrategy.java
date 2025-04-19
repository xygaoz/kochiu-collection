package com.keem.kochiu.collection.service.store;

import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.exception.CollectionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.List;

public interface ResourceStoreStrategy {

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * 保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    FileVo saveFile(UploadBo uploadBo, UserDto user, String md5, String path) throws CollectionException;

    /**
     * 下载文件
     * @param request
     * @param response
     * @param resourceId
     */
    void download(HttpServletRequest request, HttpServletResponse response, Long resourceId);

    /**
     * 删除文件
     * @param resourceId
     */
    void deleteFile(int userId, Long resourceId);

    void deleteFile(int userId, UserResource resource);

    /**
     * 移动文件
     */
    void moveFile(int userId, UserResource resource, String newPath);

    /**
     * 创建文件夹
     * @param folderPath
     * @return
     */
    boolean addFolder(String folderPath) throws CollectionException;

    /**
     * 重命名文件夹
     * @param oldFolderPath
     * @param newFolderPath
     * @return
     */
    boolean renameFolder(String oldFolderPath, String newFolderPath, boolean onlyRename) throws CollectionException;

    /**
     * 删除文件夹
     * @param folderPath
     * @return
     */
    boolean deleteFolder(String folderPath);

    /**
     * 更新文件路径
     * @param userId
     * @param targetCataId
     * @param cataId
     * @return
     */
    boolean updateResourcePath(int userId, Long targetCataId, Long cataId) throws CollectionException;

    /**
     * 更新文件路径
     * @param userId
     * @param targetCataId
     * @return
     */
    boolean updateResourcesPath(int userId, Long targetCataId, List<Long> resourceIds) throws CollectionException;
}
