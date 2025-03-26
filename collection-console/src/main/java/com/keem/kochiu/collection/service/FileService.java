package com.keem.kochiu.collection.service;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.core.convert.Convert;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.ResourceRepository;
import com.keem.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

@Slf4j
@Service
public class FileService {

    private final CollectionProperties pluServiceProperties;
    private final ResourceRepository resourceRepository;
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public FileService(CollectionProperties pluServiceProperties, ResourceRepository resourceRepository) {
        this.pluServiceProperties = pluServiceProperties;
        this.resourceRepository = resourceRepository;
    }

    /**
     * 保存文件
     * @param uploadBo
     */
    public FileVo saveFile(UploadBo uploadBo) throws CollectionException {

        //判断文件类型
        String ext = FilenameUtils.getExtension(uploadBo.getFile().getOriginalFilename());
        if(!pluServiceProperties.getUploadTypes().contains(ext)){
            throw new CollectionException("不支持的文件类型");
        }

        String userCode = CheckPermitAspect.USER_INFO.get() != null ? CheckPermitAspect.USER_INFO.get().getUserCode() : null;
        if(userCode == null){
            throw new CollectionException("非法请求。");
        }

        //读取文件到本地
        String url = "/" + userCode + "/" + dateFormat.format(System.currentTimeMillis());
        String returnUrl = "/" + dateFormat.format(System.currentTimeMillis());
        String recFilePathDir = pluServiceProperties.getUploadPath();
        File dir = new File(recFilePathDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath;
        try {
            String md5 = DigestUtil.md5Hex(DigestUtil.md5Hex(uploadBo.getFile().getBytes()));
            url += "/" + md5 + "." + ext;
            returnUrl += "/" + md5 + "." + ext;
            filePath = recFilePathDir + url;
            File outfile = new File(filePath);
            if (!outfile.exists()) {
                outfile.createNewFile();
            }
            FileUtil.writeBytes(uploadBo.getFile().getBytes(), outfile);
        }
        catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException("文件保存失败");
        }

        //判断文件是否需要生成缩略图
        String thumbUrl = null;
        String resolutionRatio = null;
        FileTypeEnum fileType = FileTypeEnum.getByValue(ext);
        if(fileType.isThumb()) {
            //生成缩略图
            String thumbFilePath = filePath.replace("." + ext, "_thumb.png");
            thumbUrl = url.replace("." + ext, "_thumb.png");
            try {
                // 读取原始图片
                BufferedImage srcImg = ImageUtil.readImageWithFallback(filePath);
                if(fileType.isResolutionRatio()){
                    resolutionRatio = srcImg.getWidth() + "x" + srcImg.getHeight();
                }

                // 生成缩略图，参数依次为：原始图片，缩略图宽度，缩略图高度，是否等比缩放
                Image thumbnail = ImgUtil.scale(srcImg, 100, 100, Color.white); // 100x100的缩略图，等比缩放
                // 保存缩略图到文件
                ImgUtil.writePng(thumbnail, Files.newOutputStream(Paths.get(thumbFilePath)));
            } catch (IOException e) {
                log.error("缩略图生成失败", e);
            }
        }

        Long resId = resourceRepository.saveResource(CheckPermitAspect.USER_INFO.get().getUserId(), uploadBo.getCategoryId(),
                uploadBo.getFile().getOriginalFilename(), url, ext, resolutionRatio,
                uploadBo.getFile().getSize(), thumbUrl);

        return FileVo.builder()
                .url("/" + resId + returnUrl)
                .size(DataSizeUtil.format(uploadBo.getFile().getSize()))
                .mimeType(fileType.getMimeType())
                .build();
    }
}
