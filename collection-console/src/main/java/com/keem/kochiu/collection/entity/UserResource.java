package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("user_resource")
public class UserResource extends BaseEntity implements Cloneable{

    @TableId(type = IdType.INPUT)
    private Long resourceId;
    private Integer userId;
    private Long cateId;
    private Long cataId;
    private String sourceFileName;
    private String title;
    private String description;
    private String filePath;
    private Integer saveType;
    private String resourceUrl;
    private Integer resourceType;
    private String thumbUrl;
    private String previewUrl;
    private Integer deleted;
    private String fileExt;
    private String resolutionRatio;
    private String thumbRatio;
    private Long size;
    private Integer share;
    private Integer star;
    private String md5;
    private LocalDateTime deleteTime;
    @TableField(exist = false)
    private String cateName;
    @TableField(exist = false)
    private String cataPath;

    @Override
    public UserResource clone() {
        try {
            return (UserResource) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
