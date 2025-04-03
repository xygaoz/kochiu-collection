package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {

    jpg(true, true, "images/jpeg", "图像"),
    jpeg(true, true, "images/jpeg", "图像"),
    gif(true, true, "images/gif", "图像"),
    bmp(true, true, "images/bmp", "图像"),
    png(true, true, "images/png", "图像"),
    webp(true, true, "images/webp", "图像"),
    pdf(true, false, "application/pdf", "文档"),
    txt(true, false, "text/plain", "文档"),
    doc(true, false, "application/msword", "文档"),
    docx(true, false, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "文档"),
    xls(true, false, "application/vnd.ms-excel", "文档"),
    xlsx(true, false, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "文档"),
    ppt(true, false, "application/vnd.ms-powerpoint", "文档"),
    pptx(true, false, "application/vnd.openxmlformats-officedocument.presentationml.presentation", "文档"),
    mp4(false, false, "video/mp4", "视频"),
    mov(false, false, "video/quicktime", "视频"),
    avi(false, false, "video/x-msvideo", "视频"),
    wav(false, false, "audio/wav", "音频"),
    mp3(false, false, "audio/mpeg", "音频"),
    flac(false, false, "audio/flac", "音频"),
    unknown(false, false, "application/octet-stream", "未知"),
    ;

    final boolean thumb;
    final boolean resolutionRatio;
    final String mimeType;
    final String desc;

    FileTypeEnum(boolean thumb, boolean resolutionRatio, String mimeType, String desc) {
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

}
