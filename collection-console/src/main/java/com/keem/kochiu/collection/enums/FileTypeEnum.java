package com.keem.kochiu.collection.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {

    jpg(true, true, "images/jpeg"),
    jpeg(true, true, "images/jpeg"),
    gif(true, true, "images/gif"),
    bmp(true, true, "images/bmp"),
    png(true, true, "images/png"),
    webp(true, true, "images/webp"),
    pdf(true, false, "application/pdf"),
    txt(true, false, "text/plain"),
    doc(true, false, "application/msword"),
    docx(true, false, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    xls(true, false, "application/vnd.ms-excel"),
    xlsx(true, false, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ppt(true, false, "application/vnd.ms-powerpoint"),
    pptx(true, false, "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    mp4(false, false, "video/mp4"),
    mov(false, false, "video/quicktime"),
    avi(false, false, "video/x-msvideo"),
    wav(false, false, "audio/wav"),
    mp3(false, false, "audio/mpeg"),
    flac(false, false, "audio/flac"),
    unknown(false, false, "application/octet-stream"),
    ;

    final boolean thumb;
    final boolean resolutionRatio;
    final String mimeType;

    FileTypeEnum(boolean thumb, boolean resolutionRatio, String mimeType) {
        this.thumb = thumb;
        this.resolutionRatio = resolutionRatio;
        this.mimeType = mimeType;
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
