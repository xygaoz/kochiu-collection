package com.keem.kochiu.collection.service.strategy;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import com.keem.kochiu.collection.util.DocumentToImageConverter;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

@Slf4j
@Service("local")
public class LocalStrategy implements ResourceStrategy {

    private final CollectionProperties pluServiceProperties;
    private final UserResourceRepository resourceRepository;
    private final SysUserRepository userRepository;
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public LocalStrategy(CollectionProperties pluServiceProperties,
                         UserResourceRepository resourceRepository,
                         SysUserRepository userRepository) {
        this.pluServiceProperties = pluServiceProperties;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    /**
     * 保存文件
     * @param uploadBo
     */
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        //判断文件类型
        String extension = FilenameUtils.getExtension(uploadBo.getFile().getOriginalFilename());
        if(!pluServiceProperties.getUploadTypes().contains(extension)){
            throw new CollectionException("不支持的文件类型");
        }

        String userCode = userDto != null ? userDto.getUserCode() : null;
        if(userCode == null){
            throw new CollectionException("非法请求。");
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
            String md5 = DigestUtil.md5Hex(DigestUtil.md5Hex(uploadBo.getFile().getBytes()));
            url += "/" + md5 + "." + extension;
            returnUrl += "/" + md5 + "." + extension;
            filePath = recFilePathDir + url;
            File outfile = new File(filePath);
            FileUtil.writeBytes(uploadBo.getFile().getBytes(), outfile);
        }
        catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException("文件保存失败");
        }

        FileTypeEnum fileType = FileTypeEnum.getByValue(extension);
        ResourceDto resourceDto = ResourceDto.builder()
                .userId(userDto.getUserId())
                .cateId(uploadBo.getCategoryId())
                .sourceFileName(uploadBo.getFile().getOriginalFilename())
                .resourceUrl(url)
                .fileExt(extension)
                .size(uploadBo.getFile().getSize())
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
        String thumbUrl;
        if(fileType.isThumb()) {
            String thumbFilePath = filePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
            thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

            try {
                switch (fileType) {
                    case jpg:
                    case jpeg:
                    case gif:
                    case bmp:
                    case png:
                    case webp:
                        String resolutionRation = createImageThumbnail(filePath, thumbFilePath, fileType);
                        resourceDto.setResolutionRatio(resolutionRation);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case pdf:
                        DocumentToImageConverter.convertPdfFirstPage(filePath, thumbFilePath);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case doc:
                    case docx:
                        DocumentToImageConverter.convertWordToImage(filePath, thumbFilePath);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case xls:
                    case xlsx:
                        DocumentToImageConverter.convertExcelToImage(filePath, thumbFilePath);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case ppt:
                    case pptx:
                        DocumentToImageConverter.convertPptFirstPage(filePath, thumbFilePath);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case txt:
                        DocumentToImageConverter.convertTxtToImage(filePath, thumbFilePath);
                        resourceDto.setThumbUrl(thumbUrl);
                        break;
                    case mp4:
                    case mov:
                    case avi:
                    case wav:
                    case mp3:
                    case flac: {
                        Resource resource = new ClassPathResource("/images/" + fileType.name() + ".png");
                        if (resource.exists()) {
                            try {
                                FileUtil.copyFile(resource.getInputStream(), new File(thumbFilePath), StandardCopyOption.REPLACE_EXISTING);
                                resourceDto.setThumbUrl(thumbUrl);
                            } catch (IOException e) {
                                log.error("缩略图生成失败", e);
                            }
                        }
                        break;
                    }
                    default:
                        Resource resource = new ClassPathResource("/images/unknown.png");
                        if(resource.exists()){
                            try {
                                FileUtil.copyFile(resource.getInputStream(), new File(thumbFilePath), StandardCopyOption.REPLACE_EXISTING);
                                resourceDto.setThumbUrl(thumbUrl);
                            }
                            catch (IOException e) {
                                log.error("缩略图生成失败", e);
                            }
                        }

                }
            }
            catch (Exception e) {
                log.error("缩略图生成失败", e);
            }
        }
    }

    /**
     * 生成图片缩略图
     * @param filePath
     * @param thumbFilePath
     * @param fileType
     * @return
     * @throws IOException
     */
    private String createImageThumbnail(String filePath, String thumbFilePath, FileTypeEnum fileType) throws IOException {

        String resolutionRation = null;
        //生成缩略图
        // 读取原始图片
        BufferedImage srcImg = ImageUtil.readImageWithFallback(filePath);
        if(fileType.isResolutionRatio()){
            resolutionRation = srcImg.getWidth() + "x" + srcImg.getHeight();
        }
        ImageUtil.writeThumbnail(srcImg, thumbFilePath);

        return resolutionRation;
    }

    /**
     * 下载文件
     * @param request
     * @param resourceId
     */
    public void download(HttpServletRequest request, HttpServletResponse response, int resourceId) {
        //请求路径
        String url = request.getRequestURI();
        url = url.substring(request.getContextPath().length());

        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            response.setStatus(404);
            return;
        }
        if(resource.getDeleted() == 1){
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
        if(!url.equals(resource.getResourceUrl())){
            response.setStatus(404);
            return;
        }

        //读取文件下载
        String filePath = pluServiceProperties.getUploadPath() + resource.getResourceUrl();
        File file = new File(filePath);
        if(!file.exists()){
            response.setStatus(404);
            return;
        }

        response.setHeader("Content-Disposition", "attachment;filename=" + resource.getSourceFileName());
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setContentType(FileTypeEnum.getByValue(resource.getResourceType()).getMimeType());
        try {
            FileUtil.writeToStream(file, response.getOutputStream());
        }
        catch (IOException e) {
            log.error("文件下载失败", e);
            response.setStatus(500);
        }
    }

    @Override
    public void deleteFile(int resourceId) {

    }
}
