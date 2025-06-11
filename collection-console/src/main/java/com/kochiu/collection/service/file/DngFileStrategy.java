package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.enums.ApiModeEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import com.kochiu.collection.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;

@Slf4j
@Service("dng")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-adobe-dng", desc = ResourceTypeEnum.IMAGE)
public class DngFileStrategy extends TifFileStrategy {

    @Autowired
    public DngFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".dng.png";
    }

    @Override
    public String createThumbnail(File file, String thumbFilePath, String thumbUrl, FileType fileType, ResourceDto resourceDto) throws Exception {
        if (!collectionProperties.getGraphicsMagick().isEnabled()) {
            return defaultThumbnail(thumbFilePath, thumbUrl, fileType, resourceDto);
        }
        return super.createThumbnail(file, thumbFilePath, thumbUrl, fileType, resourceDto);
    }

    @Override
    protected BufferedImage processImage(File file) throws Exception {
        if (!collectionProperties.getGraphicsMagick().isEnabled()) {
            return ImageUtil.readImageWithFallback(file);
        }

        if (shouldUseImageMagick(file)) {
            if (collectionProperties.getGraphicsMagick().getMode() == ApiModeEnum.REMOTE) {
                return processDngWithRemoteImageMagick(file);
            } else {
                return processDngWithLocalImageMagick(file);
            }
        }
        return super.processImage(file);
    }

    private BufferedImage processDngWithLocalImageMagick(File file) throws Exception {
        String tempOutput = File.createTempFile("dng_", ".png").getAbsolutePath();

        try {
            IMOperation op = new IMOperation();
            op.addImage(file.getAbsolutePath());
            op.quality(95.0); // DNG需要更高品质
            op.colorspace("sRGB"); // 确保色彩空间正确
            op.addImage(tempOutput);

            ConvertCmd cmd = new ConvertCmd();
            cmd.run(op);

            return ImageIO.read(new File(tempOutput));
        } finally {
            new File(tempOutput).delete();
        }
    }

    private BufferedImage processDngWithRemoteImageMagick(File file) throws Exception {
        CollectionProperties.Remote remoteConfig = collectionProperties.getGraphicsMagick().getRemote();
        String apiUrl = remoteConfig.getApiHost() + "/convert";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.IMAGE_PNG));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            body.add("outputFormat", "png");
            body.add("quality", "95");
            body.add("colorspace", "sRGB");
            body.add("noflatten", "true"); // DNG不需要合并图层

            if (remoteConfig.getUsername() != null && remoteConfig.getPassword() != null) {
                headers.setBasicAuth(remoteConfig.getUsername(), remoteConfig.getPassword());
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return ImageIO.read(new ByteArrayInputStream(response.getBody()));
            }
            throw new RuntimeException("DNG conversion failed: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Remote DNG conversion failed", e);
            throw e;
        }
    }
}