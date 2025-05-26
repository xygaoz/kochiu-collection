package com.kochiu.collection.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

import static com.kochiu.collection.enums.ResourceTypeEnum.*;

@Getter
public enum FileTypeEnum {

    jpg(true, true, "images/jpeg", IMAGE),
    jpeg(true, true, "images/jpeg", IMAGE),
    gif(true, true, "images/gif", IMAGE),
    bmp(true, true, "images/bmp", IMAGE),
    png(true, true, "images/png", IMAGE),
    webp(true, true, "images/webp", IMAGE),
    tif(true, true, "images/tiff", IMAGE),
    psd(true, true, "image/vnd.adobe.photoshop", IMAGE),
    psb(true, true, "image/vnd.adobe.photoshop", IMAGE),
    dng(true, true, "image/x-adobe-dng", IMAGE),
    nef(true, true, "image/x-nikon-nef", IMAGE),
    cr2(true, true, "image/x-canon-cr2", IMAGE),
    arw(true, true, "image/x-sony-arw", IMAGE),
    pdf(true, false, "application/pdf", DOCUMENT),
    txt(true, false, "text/plain", DOCUMENT),
    doc(true, false, "application/msword", DOCUMENT),
    docx(true, false, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOCUMENT),
    xls(true, false, "application/vnd.ms-excel", DOCUMENT),
    xlsx(true, false, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", DOCUMENT),
    ppt(true, false, "application/vnd.ms-powerpoint", DOCUMENT),
    pptx(true, false, "application/vnd.openxmlformats-officedocument.presentationml.presentation", DOCUMENT),
    mp4(true, false, "video/mp4", VIDEO),
    mov(true, false, "video/quicktime", VIDEO),
    avi(true, false, "video/x-msvideo", VIDEO),
    wav(false, false, "audio/wav", AUDIO),
    mp3(false, false, "audio/mpeg", AUDIO),
    flac(false, false, "audio/flac", AUDIO),
    unknown(false, false, "application/octet-stream", UNKNOWN),
    ;

    // 是否生成缩略图，否的话，使用系统默认的
    final boolean thumb;
    final boolean resolutionRatio;
    final String mimeType;
    final ResourceTypeEnum desc;

    FileTypeEnum(boolean thumb, boolean resolutionRatio, String mimeType, ResourceTypeEnum desc) {
        this.thumb = thumb;
        this.resolutionRatio = resolutionRatio;
        this.mimeType = mimeType;
        this.desc = desc;
    }

    public static FileTypeEnum getByValue(String name) {
        for (FileTypeEnum resourceTypeEnum : values()) {
            if (resourceTypeEnum.name().equalsIgnoreCase(name)) {
                return resourceTypeEnum;
            }
        }
        return unknown;
    }

    public static FileTypeEnum[] getByDesc(ResourceTypeEnum resourceType) {
        return Arrays.stream(values()).filter(fileTypeEnum -> fileTypeEnum.desc == resourceType).toArray(FileTypeEnum[]::new);
    }

    public static List<String> getNames(ResourceTypeEnum resourceType){
        return Arrays.stream(getByDesc(resourceType)).map(FileTypeEnum::name).toList();
    }
}
