package com.kochiu.collection.data.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChunkUploadDto {
    private String fileId;          // 文件唯一标识
    private Integer chunkIndex;     // 当前分片序号
    private Integer totalChunks;    // 总分片数
    private Long chunkSize;         // 分片大小
    private Long totalSize;         // 文件总大小
    private String originalName;    // 原始文件名
    private MultipartFile file;     // 分片文件
    private Long categoryId;     // 分类ID
    private Boolean overwrite;       // 是否覆盖
    private Long cataId;         // 目录ID
    private Boolean autoCreate;     // 是否自动创建目录
}