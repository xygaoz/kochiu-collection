package com.keem.kochiu.collection.service.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCatalogRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.service.file.FileStrategy;
import com.keem.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;

@Slf4j
@Service("local")
public class LocalStoreStrategy implements ResourceStoreStrategy {

    private final CollectionProperties collectionProperties;
    private final UserResourceRepository resourceRepository;
    private final SysUserRepository userRepository;
    private final FileStrategyFactory fileStrategyFactory;
    private final UserCatalogRepository catalogRepository;

    public LocalStoreStrategy(CollectionProperties pluServiceProperties,
                              UserResourceRepository resourceRepository,
                              SysUserRepository userRepository,
                              FileStrategyFactory fileStrategyFactory,
                              UserCatalogRepository catalogRepository) {
        this.collectionProperties = pluServiceProperties;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStrategyFactory = fileStrategyFactory;
        this.catalogRepository = catalogRepository;
    }

    /**
     * 保存文件
     * @param uploadBo
     */
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto, String md5, String path) throws CollectionException {

        //判断文件类型
        String extension = FilenameUtils.getExtension(uploadBo.getFile().getOriginalFilename());
        if(!collectionProperties.getUploadTypes().contains(extension)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        //读取文件到本地
        String returnUrl = path.substring(("/" + userCode).length());
        String recFilePathDir = collectionProperties.getUploadPath();
        File dir = new File(recFilePathDir + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath;
        try {
            path += "/" + md5 + "." + extension;
            returnUrl += "/" + md5 + "." + extension;
            filePath = recFilePathDir + path;
            File outfile = new File(filePath);
            FileUtil.writeBytes(uploadBo.getFile().getBytes(), outfile);
        }
        catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException(FILE_SAVING_FAILURE);
        }

        FileTypeEnum fileType = FileTypeEnum.getByValue(extension);
        ResourceDto resourceDto = ResourceDto.builder()
                .userId(userDto.getUserId())
                .cateId(uploadBo.getCategoryId())
                .cataId(uploadBo.getCataId())
                .sourceFileName(uploadBo.getFile().getOriginalFilename())
                .resourceUrl(path)
                .fileExt(extension)
                .size(uploadBo.getFile().getSize())
                .md5(md5)
               .build();
        //生成缩略图
        createThumbnail(resourceDto, fileType, filePath);

        Long resId = resourceRepository.saveResource(resourceDto);

        String thumbUrl = resourceDto.getThumbUrl();
        if(thumbUrl != null){
            thumbUrl = thumbUrl.replace("/" + userCode + "/", "");
            thumbUrl = "/" + resId + "/" + thumbUrl;
        }

        return FileVo.builder()
                .url("/" + resId + returnUrl)
                .thumbnailUrl(thumbUrl)
                .size(DataSizeUtil.format(uploadBo.getFile().getSize()))
                .mimeType(fileType.getMimeType())
                .build();
    }

    /**
     * 生成缩略图
     * @param resourceDto
     * @param fileType
     * @param filePath
     */
    private void createThumbnail(ResourceDto resourceDto, FileTypeEnum fileType, String filePath){

        //判断文件是否需要生成缩略图
        if(fileType.isThumb()) {
            String thumbFilePath = filePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
            String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

            FileStrategy fileStrategy = fileStrategyFactory.getStrategy(fileType);
            try {
                fileStrategy.createThumbnail(filePath, thumbFilePath, thumbUrl, fileType, resourceDto);
            } catch (Exception e) {
                log.error("缩略图生成失败", e);
            }
        }
    }

    /**
     * 下载文件
     * @param request
     * @param resourceId
     */
    public void download(HttpServletRequest request, HttpServletResponse response, Long resourceId) {
        //请求路径
        String url = request.getRequestURI();
        url = url.substring(request.getContextPath().length());

        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            response.setStatus(404);
            return;
        }

        String str = "/" + resourceId + "/";
        url = url.substring(url.indexOf(str) + str.length() - 1);
        SysUser user = userRepository.getById(resource.getUserId());
        if(user == null){
            response.setStatus(404);
            return;
        }
        url = "/" + user.getUserCode() + url;
        if(!url.equals(resource.getResourceUrl())
                && !url.equals(resource.getThumbUrl())
                && !url.equals(resource.getPreviewUrl())){
            response.setStatus(404);
            return;
        }

        //读取文件下载
        String filePath = collectionProperties.getUploadPath() + url;
        File file = new File(filePath);
        if(!file.exists()){
            response.setStatus(404);
            return;
        }

        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setContentType(FileTypeEnum.getByValue(resource.getResourceType()).getMimeType());
        if(url.equals(resource.getResourceUrl())){
            if(HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())){
                response.setHeader("Content-Disposition", "attachment;filename=" +
                        URLEncoder.encode(resource.getSourceFileName(), StandardCharsets.UTF_8));
            }
            else{
                response.setHeader("Content-Disposition", "inline;filename=" +
                    URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
            }
        }
        try {
            FileUtil.writeToStream(file, response.getOutputStream());
        }
        catch (IOException e) {
            log.error("文件下载失败", e);
            response.setStatus(500);
        }
    }

    /**
     * 删除文件
     * @param userId
     * @param resourceId
     */
    @Override
    public void deleteFile(int userId, Long resourceId) {
        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            return;
        }
        if(resource.getUserId() != userId){
            return;
        }

        File file = new File(collectionProperties.getUploadPath() + resource.getResourceUrl());
        if(file.exists()){
            file.delete();
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(collectionProperties.getUploadPath() + resource.getThumbUrl());
            if (file.exists()) {
                file.delete();
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(collectionProperties.getUploadPath() + resource.getPreviewUrl());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public void moveFile(int userId, UserResource resource, String newPath) {
        //查找资源
        if(resource == null){
            return;
        }
        if(resource.getUserId() != userId){
            return;
        }

        File dir = new File(collectionProperties.getUploadPath() + newPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(collectionProperties.getUploadPath() + resource.getResourceUrl());
        if(file.exists()){
            FileUtil.move(file, dir, true);
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(collectionProperties.getUploadPath() + resource.getThumbUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(collectionProperties.getUploadPath() + resource.getPreviewUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }
    }

    @Override
    public boolean addFolder(String folderPath) throws CollectionException {
        File file = new File(collectionProperties.getUploadPath() + folderPath);
        if(!file.exists()){
            if(!file.mkdirs()){
                throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
            }
        }

        return true;
    }

    @Override
    public boolean renameFolder(String oldFolderPath, String newFolderPath, boolean onlyRename) throws CollectionException {
        if(onlyRename){
            String[] paths = StringUtils.split(newFolderPath, "/");
            if(new File(collectionProperties.getUploadPath() + newFolderPath).exists()){
                throw new CollectionException(ErrorCodeEnum.CATALOG_IS_EXIST);
            }
            try {
                FileUtil.rename(new File(collectionProperties.getUploadPath() + oldFolderPath), paths[paths.length - 1], false);
                return true;
            }
            catch (Exception e) {
                log.error("系统错误", e);
                return false;
            }
        }
        else{
            if(new File(collectionProperties.getUploadPath() + newFolderPath).exists()){
                //先复制原目录文件过去
                try {
                    FileUtil.copyFilesFromDir(new File(collectionProperties.getUploadPath() + oldFolderPath), new File(collectionProperties.getUploadPath() + newFolderPath), true);
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
                try {
                    FileUtil.del(new File(collectionProperties.getUploadPath() + oldFolderPath));
                    return true;
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
            }
            else {
                try {
                    FileUtil.move(new File(collectionProperties.getUploadPath() + oldFolderPath), new File(collectionProperties.getUploadPath() + newFolderPath), false);
                    return true;
                } catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
            }
        }
    }

    @Override
    public boolean deleteFolder(String folderPath) {
        return FileUtil.del(new File(collectionProperties.getUploadPath() + folderPath));
    }

    @Override
    public boolean updateResourcePath(int userId, Long targetCataId, Long cataId) throws CollectionException {
        //先取出该目录下的资源
        List<UserResource> userResources = resourceRepository.list(new LambdaQueryWrapper<UserResource>()
                .eq(UserResource::getUserId, userId)
                .eq(UserResource::getCataId, cataId)
        );
        List<Long> resourceIds = userResources.stream().map(UserResource::getResourceId).toList();
        return updateResourcesPath(userId, targetCataId, resourceIds);
    }

    @Override
    public boolean updateResourcesPath(int userId, Long targetCataId, List<Long> resourceIds) throws CollectionException {
        SysUser user = userRepository.getById(userId);
        UserCatalog parentCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, userId)
                .eq(UserCatalog::getCataId, targetCataId));
        if (parentCatalog == null) {
            throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_INVALID);
        }

        // 更新数据库
        //逐个更新
        for (Long resourceId : resourceIds) {
            UserResource userResource = resourceRepository.getById(resourceId);
            if(userResource == null){
                throw new CollectionException(ErrorCodeEnum.RESOURCE_NOT_EXIST);
            }
            if(userResource.getCataId().equals(parentCatalog.getCataId())){
                throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_SAME);
            }

            userResource.setCataId(parentCatalog.getCataId());
            //资源路径
            //截取文件名
            String fileName = userResource.getFilePath().substring(userResource.getResourceUrl().lastIndexOf("/") + 1);
            userResource.setFilePath("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName);
            if(userResource.getResourceUrl() != null){
                fileName = userResource.getResourceUrl().substring(userResource.getResourceUrl().lastIndexOf("/") + 1);
                userResource.setResourceUrl("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName);
            }
            if(userResource.getThumbUrl() != null) {
                fileName = userResource.getThumbUrl().substring(userResource.getThumbUrl().lastIndexOf("/") + 1);
                userResource.setThumbUrl("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName);
            }
            if(userResource.getPreviewUrl() != null) {
                fileName = userResource.getPreviewUrl().substring(userResource.getPreviewUrl().lastIndexOf("/") + 1);
                userResource.setPreviewUrl("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName);
            }
            if(!resourceRepository.updateById(userResource)){
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }
        }
        return false;
    }
}
