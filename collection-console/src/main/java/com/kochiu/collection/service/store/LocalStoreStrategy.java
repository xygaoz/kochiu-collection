package com.kochiu.collection.service.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.FileVo;
import com.kochiu.collection.entity.SysStrategy;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.enums.SaveTypeEnum;
import com.kochiu.collection.enums.StrategyEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysStrategyRepository;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserCatalogRepository;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.SysStrategyService;
import com.kochiu.collection.service.SystemService;
import com.kochiu.collection.service.file.FileStrategy;
import com.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.kochiu.collection.enums.ErrorCodeEnum.*;

@Slf4j
@Service("local")
public class LocalStoreStrategy implements ResourceStoreStrategy {

    private final UserResourceRepository resourceRepository;
    private final SysUserRepository userRepository;
    private final FileStrategyFactory fileStrategyFactory;
    private final UserCatalogRepository catalogRepository;
    private final ThumbnailService thumbnailService;
    private final SystemService systemService;
    private final SysStrategyService sysStrategyService;
    private final SysStrategyRepository strategyRepository;

    public LocalStoreStrategy(UserResourceRepository resourceRepository,
                              SysUserRepository userRepository,
                              FileStrategyFactory fileStrategyFactory,
                              UserCatalogRepository catalogRepository,
                              SysStrategyRepository strategyRepository,
                              ThumbnailService thumbnailService,
                              SystemService systemService,
                              SysStrategyService sysStrategyService) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStrategyFactory = fileStrategyFactory;
        this.catalogRepository = catalogRepository;
        this.thumbnailService = thumbnailService;
        this.systemService = systemService;
        this.sysStrategyService = sysStrategyService;
        this.strategyRepository = strategyRepository;
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

        if(!sysStrategyService.checkLocalStrategy()){
            throw new CollectionException(SERVER_PATH_ERROR);
        }

        //判断文件类型
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        FileType fileType = fileStrategyFactory.getFileType(extension);
        if(fileType.desc().equals(ResourceTypeEnum.UNKNOWN)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        //读取文件到本地
        String returnUrl = savePath.substring(("/" + userCode).length());
        String recFilePathDir = getServerUrl();
        //检查目标路径
        if(systemService.isSensitivePath(recFilePathDir)){
            throw new CollectionException(SENSITIVE_PATH);
        }

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

        // 保存资源记录
        Long resId = resourceRepository.saveResource(resourceDto, fileType.desc().getCode());
        resourceDto.setResourceId(resId);

        // 立即返回结果（不等待缩略图生成）
        String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");
        thumbUrl = thumbUrl.replace("/" + userCode + "/", "");
        thumbUrl = "/" + resId + "/" + thumbUrl;

        log.debug("保存文件成功 - 文件名: {}, 文件大小: {}", originalFilename, size);
        return FileVo.builder()
                .resourceId(resId)
                .url("/" + resId + returnUrl)
                .thumbnailUrl(thumbUrl)
                .resourceUrl(savePath)
                .size(DataSizeUtil.format(size))
                .mimeType(fileType.mimeType())
                .build();
    }

    public void asyncCreateThumbnail(Long resourceId){

        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            return;
        }
        ResourceDto resourceDto = ResourceDto.builder()
                .resourceId(resourceId)
                .resourceUrl(resource.getResourceUrl())
                .fileExt(resource.getFileExt())
                .filePath(resource.getFilePath())
                .build();

        FileType fileType = fileStrategyFactory.getFileType(resource.getFileExt());

        String recFilePathDir = getServerUrl();
        String filePath = recFilePathDir + resource.getFilePath();

        thumbnailService.asyncCreateThumbnail(resourceDto, fileType, filePath, null, null);
    }

    @Override
    public boolean checkServerUrl() {
        return sysStrategyService.checkLocalStrategy();
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
        FileType fileType = fileStrategyFactory.getFileType(extension);
        if(fileType.desc().equals(ResourceTypeEnum.UNKNOWN)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        String resourceUrl = "/" + userCode + savePath + "/" + md5 + "." + extension;
        String recFilePathDir = getServerUrl();
        File dir = new File(recFilePathDir + "/" + userCode + savePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        long size = FileUtil.size(file);

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

        Long resId = resourceRepository.saveResource(resourceDto, fileType.desc().getCode());
        resourceDto.setResourceId(resId);

        //生成缩略图
        createThumbnail(resourceDto, fileType, file.getAbsolutePath(), recFilePathDir + resourceUrl);
    }

    private void createThumbnail(ResourceDto resourceDto, FileType fileType, String sourceFile, String thumbFilePath){

        //判断文件是否需要生成缩略图
        if(fileType.thumb()) {
            thumbFilePath = thumbFilePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
            String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

            thumbnailService.asyncCreateThumbnail(resourceDto, fileType, sourceFile, thumbFilePath, thumbUrl);
        }
    }

    public ResponseEntity<Resource> downloadResource(
            HttpServletRequest request,
            HttpServletResponse response,
            List<HttpRange> ranges,
            Long resourceId) {

        try {
            // 1. 获取请求路径和资源信息
            String url = request.getRequestURI().substring(request.getContextPath().length());

            UserResource resource = resourceRepository.getById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 验证路径和用户
            String str = "/" + resourceId + "/";
            url = url.substring(url.indexOf(str) + str.length() - 1);
            SysUser user = userRepository.getById(resource.getUserId());
            if (user == null) {
                return ResponseEntity.notFound().build();
            } else {
                url = "/" + user.getUserCode() + url;
                if (!url.equals(resource.getResourceUrl()) &&
                        !url.equals(resource.getThumbUrl()) &&
                        !url.equals(resource.getPreviewUrl())) {
                    return ResponseEntity.notFound().build();
                }
            }

            // 3. 获取实际文件
            String filePath = getServerUrl() + url;
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 4. 获取文件类型
            String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
            FileType fileType = fileStrategyFactory.getFileType(extension);
            MediaType mediaType = MediaType.parseMediaType(fileType.mimeType());

            // 5. 准备公共响应头
            String disposition = HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())
                    ? "attachment"
                    : "inline";
            String filenameHeader = disposition + ";filename=" +
                    URLEncoder.encode(resource.getSourceFileName(), StandardCharsets.UTF_8);

            // 6. 处理Range请求
            if (ranges != null && !ranges.isEmpty()) {
                HttpRange range = ranges.get(0);
                long fileLength = file.length();
                long start = range.getRangeStart(fileLength);
                long end = range.getRangeEnd(fileLength);
                end = Math.min(end, fileLength - 1);

                long contentLength = end - start + 1;
                InputStream inputStream = new FileInputStream(file);
                inputStream.skip(start);

                InputStreamResource resourceStream = new InputStreamResource(inputStream) {
                    @Override
                    public long contentLength() {
                        return contentLength;
                    }
                };

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, filenameHeader)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .contentLength(contentLength)
                        .body(resourceStream);
            }

            // 7. 完整文件下载
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, filenameHeader)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentLength(file.length())
                    .body(new FileSystemResource(file));

        } catch (IOException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new ByteArrayResource("文件下载失败".getBytes(StandardCharsets.UTF_8)));
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

        //假如是本地资源，不能删除
        if(resource.getSaveType() != SaveTypeEnum.LOCAL.getCode()){
            return;
        }

        String serverUrl = getServerUrl();
        File file = new File(serverUrl + resource.getResourceUrl());
        if(file.exists()){
            file.delete();
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(serverUrl + resource.getThumbUrl());
            if (file.exists()) {
                file.delete();
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(serverUrl + resource.getPreviewUrl());
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

        String serverUrl = getServerUrl();
        File dir = new File(serverUrl + newPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            File file = new File(serverUrl + resource.getThumbUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            File file = new File(serverUrl + resource.getPreviewUrl());
            if (file.exists()) {
                FileUtil.move(file, dir, true);
            }
        }

        //假如是本地资源，不能移动原资源文件
        if(resource.getSaveType() != SaveTypeEnum.LOCAL.getCode()){
            return;
        }

        File file = new File(serverUrl + resource.getResourceUrl());
        if(file.exists()){
            FileUtil.move(file, dir, true);
        }
    }

    @Override
    public boolean addFolder(String folderPath) throws CollectionException {
        File file = new File(getServerUrl() + folderPath);
        if(!file.exists()){
            if(!file.mkdirs()){
                throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
            }
        }

        return true;
    }

    @Override
    public boolean renameFolder(String oldFolderPath, String newFolderPath, boolean onlyRename) throws CollectionException {

        String serverUrl = getServerUrl();
        if(onlyRename){
            String[] paths = StringUtils.split(newFolderPath, "/");
            if(new File(serverUrl + newFolderPath).exists()){
                throw new CollectionException(ErrorCodeEnum.CATALOG_IS_EXIST);
            }
            try {
                FileUtil.rename(new File(serverUrl + oldFolderPath), paths[paths.length - 1], false);
                return true;
            }
            catch (Exception e) {
                log.error("系统错误", e);
                return false;
            }
        }
        else{
            if(new File(serverUrl + newFolderPath).exists()){
                //先复制原目录文件过去
                try {
                    FileUtil.copyFilesFromDir(new File(serverUrl + oldFolderPath), new File(serverUrl + newFolderPath), true);
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
                try {
                    FileUtil.del(new File(serverUrl + oldFolderPath));
                    return true;
                }
                catch (Exception e) {
                    log.error("系统错误", e);
                    return false;
                }
            }
            else {
                try {
                    FileUtil.move(new File(serverUrl + oldFolderPath), new File(serverUrl + newFolderPath), false);
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
        return FileUtil.del(new File(getServerUrl() + folderPath));
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

    //获取服务器地址
    public String getServerUrl() {
        SysStrategy strategy;
        try {
            strategy = strategyRepository.getOne(new LambdaQueryWrapper<SysStrategy>()
                    .eq(SysStrategy::getStrategyCode, StrategyEnum.LOCAL.getCode())
                    .last("limit 1")
            );
        } catch (Exception e) {
            throw new RuntimeException(LOCAL_STRATEGY_IS_INVALID.getMessage());
        }
        if(strategy == null){
            throw new RuntimeException(LOCAL_STRATEGY_IS_INVALID.getMessage());
        }
        return strategy.getServerUrl();
    }
}
