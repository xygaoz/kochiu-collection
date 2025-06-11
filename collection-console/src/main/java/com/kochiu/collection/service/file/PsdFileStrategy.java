package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.properties.CollectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
@Service("psd")
@FileType(thumb = true, resolutionRatio = true, mimeType = "application/x-photoshop", desc = ResourceTypeEnum.IMAGE)
public class PsdFileStrategy extends TifFileStrategy {

    @Autowired
    public PsdFileStrategy(CollectionProperties collectionProperties) {
        super(collectionProperties);
    }

    @Override
    protected String getPreviewExtension() {
        return ".psd.png";
    }

    @Override
    protected boolean shouldUseImageMagick(File file) {
        // PSD文件总是使用ImageMagick处理
        return collectionProperties.getGraphicsMagick().isEnabled();
    }

    @Override
    protected BufferedImage processImage(File file) throws Exception {
        // PSD需要特殊处理，直接调用父类的ImageMagick处理
        return super.processImage(file);
    }

    @Override
    protected void generatePreviewWithLocalImageMagick(File inputFile, String outputPath) throws Exception {
        IMOperation op = new IMOperation();
        op.addImage(inputFile.getAbsolutePath() + "[0]"); // 处理所有图层
        op.flatten(); // 强制合并PSD图层
        op.resize(2048, 2048);
        op.quality(100.0);
        op.addImage(outputPath);

        ConvertCmd cmd = new ConvertCmd();
        cmd.run(op);
    }
}