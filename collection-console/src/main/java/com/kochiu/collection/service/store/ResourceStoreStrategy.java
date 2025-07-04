package com.kochiu.collection.service.store;

import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.FileVo;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.exception.CollectionException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

public interface ResourceStoreStrategy {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * 保存文件
     * @return
     * @throws CollectionException
     */
    FileVo saveFile(InputStream fileInputStream,
                    String originalFilename,
                    SysUser userDto,
                    String md5,
                    String savePath,
                    Long categoryId,
                    Long cataId) throws CollectionException;

    /**
     * 直接保存资源文件
     * @param file
     * @param user
     * @param md5
     * @param savePath
     * @param categoryId
     * @param cataId
     * @throws CollectionException
     */
    void saveLinkResource(File file,
                          SysUser user,
                          String md5,
                          String savePath,
                          Long categoryId,
                          Long cataId) throws CollectionException;

    /**
     * 统一文件下载方法
     * @param request HTTP请求
     * @param response HTTP响应
     * @param ranges Range请求头（可为空）
     * @param resourceId 资源ID
     * @return ResponseEntity（用于媒体文件）或null（普通文件直接写入response）
     */
    ResponseEntity<Resource> downloadResource(
            HttpServletRequest request,
            HttpServletResponse response,
            List<HttpRange> ranges,
            Long resourceId);

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

    // 检查服务url是否正常
    boolean checkServerUrl();

    String getServerUrl();
}
