package com.kochiu.collection.service.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.FileVo;
import com.kochiu.collection.entity.SysStrategy;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.FileTypeEnum;
import com.kochiu.collection.enums.SaveTypeEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.repository.SysStrategyRepository;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserCatalogRepository;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.file.FileStrategy;
import com.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.kochiu.collection.enums.ErrorCodeEnum.*;

@Slf4j
@Service("local")
public class LocalStoreStrategy implements ResourceStoreStrategy {

    private final CollectionProperties collectionProperties;
    private final UserResourceRepository resourceRepository;
    private final SysUserRepository userRepository;
    private final FileStrategyFactory fileStrategyFactory;
    private final UserCatalogRepository catalogRepository;
    private final SysStrategy strategy;
    private final ThumbnailService thumbnailService;

    public LocalStoreStrategy(CollectionProperties collectionProperties,
                              UserResourceRepository resourceRepository,
                              SysUserRepository userRepository,
                              FileStrategyFactory fileStrategyFactory,
                              UserCatalogRepository catalogRepository,
                              SysStrategyRepository strategyRepository,
                              ThumbnailService thumbnailService) {
        this.collectionProperties = collectionProperties;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStrategyFactory = fileStrategyFactory;
        this.catalogRepository = catalogRepository;
        this.thumbnailService = thumbnailService;

        try {
            strategy = strategyRepository.getOne(new LambdaQueryWrapper<SysStrategy>()
                    .eq(SysStrategy::getStrategyCode, "local")
                    .last("limit 1")
            );
        } catch (Exception e) {
            throw new RuntimeException(LOCAL_STRATEGY_IS_INVALID.getMessage());
        }
        if(strategy == null){
            throw new RuntimeException(LOCAL_STRATEGY_IS_INVALID.getMessage());
        }
    }

    /**
     * 保存文件
     */
    public FileVo saveFile(InputStream fileInputStream,
                           String originalFilename,
                           UserDto userDto,
                           String md5,
                           String savePath,
                           Long categoryId,
                           Long cataId) throws CollectionException {

        //判断文件类型
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if(!collectionProperties.getUploadTypes().contains(extension)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        //读取文件到本地
        String returnUrl = savePath.substring(("/" + userCode).length());
        String recFilePathDir = strategy.getServerUrl();
        File dir = new File(recFilePathDir + savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath;
        savePath += "/" + md5 + "." + extension;
        returnUrl += "/" + md5 + "." + extension;
        filePath = recFilePathDir + savePath;
        File outfile = new File(filePath);
        FileUtil.writeFromStream(fileInputStream, outfile);
        long size = FileUtil.size(outfile);
        if(size == 0) {
            throw new CollectionException(ErrorCodeEnum.FILE_SAVING_FAILURE);
        }

        FileTypeEnum fileType = FileTypeEnum.getByValue(extension);
        ResourceDto resourceDto = ResourceDto.builder()
                .userId(userDto.getUserId())
                .cateId(categoryId)
                .cataId(cataId)
                .sourceFileName(originalFilename)
                .resourceUrl(savePath)
                .fileExt(extension)
                .size(size)
                .md5(md5)
                .saveType(SaveTypeEnum.LOCAL)
               .build();

        Long resId = resourceRepository.saveResource(resourceDto);

        //异步生成缩略图
        resourceDto.setResourceId(resId);
        thumbnailService.asyncCreateThumbnail(resourceDto, fileType, filePath);

        //拼接缩略图url
        String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");
        thumbUrl = thumbUrl.replace("/" + userCode + "/", "");
        thumbUrl = "/" + resId + "/" + thumbUrl;

        return FileVo.builder()
                .url("/" + resId + returnUrl)
                .thumbnailUrl(thumbUrl)
                .resourceUrl(savePath)
                .size(DataSizeUtil.format(size))
                .mimeType(fileType.getMimeType())
                .build();
    }

    /**
     * 直接保存资源
     * @param file
     * @param userDto
     * @param md5
     * @param savePath
     * @param categoryId
     * @param cataId
     * @throws CollectionException
     */
    public void saveLinkResource(File file,
                                 UserDto userDto,
                                 String md5,
                                 String savePath,
                                 Long categoryId,
                                 Long cataId
                             ) throws CollectionException {

        //判断文件类型
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if(!collectionProperties.getUploadTypes().contains(extension)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        String resourceUrl = "/" + userCode + savePath + "/" + md5 + "." + extension;
        String recFilePathDir = strategy.getServerUrl();
        File dir = new File(recFilePathDir + "/" + userCode + savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long size = FileUtil.size(file);

        FileTypeEnum fileType = FileTypeEnum.getByValue(extension);
        ResourceDto resourceDto = ResourceDto.builder()
                .userId(userDto.getUserId())
                .cateId(categoryId)
                .cataId(cataId)
                .filePath(file.getAbsolutePath())
                .sourceFileName(file.getName())
                .resourceUrl(resourceUrl)
                .fileExt(extension)
                .size(size)
                .md5(md5)
                .saveType(SaveTypeEnum.LINK)
                .build();
        //生成缩略图
        createThumbnail(resourceDto, fileType, file.getAbsolutePath(), recFilePathDir + resourceUrl);

        resourceRepository.saveResource(resourceDto);
    }

    private void createThumbnail(ResourceDto resourceDto, FileTypeEnum fileType, String sourceFile, String thumbFilePath){

        //判断文件是否需要生成缩略图
        if(fileType.isThumb()) {
            thumbFilePath = thumbFilePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
            String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

            FileStrategy fileStrategy = fileStrategyFactory.getStrategy(fileType);
            try {
                fileStrategy.createThumbnail(new File(sourceFile), thumbFilePath, thumbUrl, fileType, resourceDto);
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
        String filePath = strategy.getServerUrl() + url;
        File file = new File(filePath);
        if(!file.exists()){
            response.setStatus(404);
            return;
        }

        String fileName = file.getName();
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();

        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setContentType(FileTypeEnum.getByValue(extension).getMimeType());
        log.debug("下载文件：{}, mimeType: {}", url, FileTypeEnum.getByValue(extension).getMimeType());
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
        deleteFile(userId, resource);
    }

    @Override
    public void deleteFile(int userId, UserResource resource) {
        if(resource == null){
            return;
        }
        if(resource.getUserId() != userId){
            return;
        }

        File file = new File(strategy.getServerUrl() + resource.getResourceUrl());
        if(file.exists()){
            file.delete();
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(strategy.getServerUrl() + resource.getThumbUrl());
            if (file.exists()) {
                file.delete();
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(strategy.getServerUrl() + resource.getPreviewUrl());
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

        File dir = new File(strategy.getServerUrl() + newPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(strategy.getServerUrl() + resource.getResourceUrl());
        if(file.exists()){
            FileUtil.move(file, dir, true);
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(strategy.getServerUrl() + resource.getThumbUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(strategy.getServerUrl() + resource.getPreviewUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }
    }

    @Override
    public boolean addFolder(String folderPath) throws CollectionException {
        File file = new File(strategy.getServerUrl() + folderPath);
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
            if(new File(strategy.getServerUrl() + newFolderPath).exists()){
                throw new CollectionException(ErrorCodeEnum.CATALOG_IS_EXIST);
            }
            try {
                FileUtil.rename(new File(strategy.getServerUrl() + oldFolderPath), paths[paths.length - 1], false);
                return true;
            }
            catch (Exception e) {
                log.error("系统错误", e);
                return false;
            }
        }
        else{
            if(new File(strategy.getServerUrl() + newFolderPath).exists()){
                //先复制原目录文件过去
                try {
                    FileUtil.copyFilesFromDir(new File(strategy.getServerUrl() + oldFolderPath), new File(strategy.getServerUrl() + newFolderPath), true);
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
                try {
                    FileUtil.del(new File(strategy.getServerUrl() + oldFolderPath));
                    return true;
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
            }
            else {
                try {
                    FileUtil.move(new File(strategy.getServerUrl() + oldFolderPath), new File(strategy.getServerUrl() + newFolderPath), false);
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
        return FileUtil.del(new File(strategy.getServerUrl() + folderPath));
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

            userResource.setCataId(parentCatalog.getCataId());
            //资源路径
            //截取文件名
            String fileName = userResource.getFilePath().substring(userResource.getResourceUrl().lastIndexOf("/") + 1);
            userResource.setFilePath(normPath("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName));
            if(userResource.getResourceUrl() != null){
                fileName = userResource.getResourceUrl().substring(userResource.getResourceUrl().lastIndexOf("/") + 1);
                userResource.setResourceUrl(normPath("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName));
            }
            if(userResource.getThumbUrl() != null) {
                fileName = userResource.getThumbUrl().substring(userResource.getThumbUrl().lastIndexOf("/") + 1);
                userResource.setThumbUrl(normPath("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName));
            }
            if(userResource.getPreviewUrl() != null) {
                fileName = userResource.getPreviewUrl().substring(userResource.getPreviewUrl().lastIndexOf("/") + 1);
                userResource.setPreviewUrl(normPath("/" + user.getUserCode() + parentCatalog.getCataPath() + "/" + fileName));
            }
            if(!resourceRepository.updateById(userResource)){
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }
        }
        return false;
    }

    private String normPath(String path){
        return path.replaceAll("\\\\", "/").replaceAll("//", "/");
    }
}
