package com.keem.kochiu.collection.service.store;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.ResourceDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.service.file.FileStrategy;
import com.keem.kochiu.collection.service.file.FileStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;

@Slf4j
@Service("local")
public class LocalStoreStrategy implements ResourceStoreStrategy {

    private final CollectionProperties pluServiceProperties;
    private final UserResourceRepository resourceRepository;
    private final SysUserRepository userRepository;
    private final FileStrategyFactory fileStrategyFactory;
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public LocalStoreStrategy(CollectionProperties pluServiceProperties,
                              UserResourceRepository resourceRepository,
                              SysUserRepository userRepository, FileStrategyFactory fileStrategyFactory) {
        this.pluServiceProperties = pluServiceProperties;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.fileStrategyFactory = fileStrategyFactory;
    }

    /**
     * 保存文件
     * @param uploadBo
     */
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto, String md5) throws CollectionException {

        //判断文件类型
        String extension = FilenameUtils.getExtension(uploadBo.getFile().getOriginalFilename());
        if(!pluServiceProperties.getUploadTypes().contains(extension)){
            throw new CollectionException(UNSUPPORTED_FILE_TYPES);
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException(ILLEGAL_REQUEST);
        }

        //读取文件到本地
        String url = "/" + userCode + "/" + dateFormat.format(System.currentTimeMillis());
        String returnUrl = "/" + dateFormat.format(System.currentTimeMillis());
        String recFilePathDir = pluServiceProperties.getUploadPath();
        File dir = new File(recFilePathDir + url);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath;
        try {
            url += "/" + md5 + "." + extension;
            returnUrl += "/" + md5 + "." + extension;
            filePath = recFilePathDir + url;
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
                .sourceFileName(uploadBo.getFile().getOriginalFilename())
                .resourceUrl(url)
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
        String filePath = pluServiceProperties.getUploadPath() + url;
        File file = new File(filePath);
        if(!file.exists()){
            response.setStatus(404);
            return;
        }

        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setContentType(FileTypeEnum.getByValue(resource.getResourceType()).getMimeType());
        String ext = FilenameUtils.getExtension(file.getName());
        if(url.equals(resource.getResourceUrl())){
            if(!FileTypeEnum.getByValue(ext).getMimeType().startsWith("images/")){
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

        File file = new File(pluServiceProperties.getUploadPath() + resource.getResourceUrl());
        if(file.exists()){
            file.delete();
        }
        if(StringUtils.isNotBlank(resource.getThumbUrl())) {
            file = new File(pluServiceProperties.getUploadPath() + resource.getThumbUrl());
            if (file.exists()) {
                file.delete();
            }
        }
        if(StringUtils.isNotBlank(resource.getPreviewUrl())) {
            file = new File(pluServiceProperties.getUploadPath() + resource.getPreviewUrl());
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
