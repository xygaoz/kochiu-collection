package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_resource")
public class UserResource extends BaseEntity{

    @TableId(type = IdType.INPUT)
    private Long resourceId;
    private Integer userId;
    private Long cateId;
    private String sourceFileName;
    private String title;
    private String description;
    private String filePath;
    private Integer saveType;
    private String resourceUrl;
    private String resourceType;
    private String thumbUrl;
    private String previewUrl;
    private Integer deleted;
    private String fileExt;
    private String resolutionRatio;
    private String thumbRatio;
    private Long size;
    private Integer isPublic;
    private Integer star;
    private String md5;
}
