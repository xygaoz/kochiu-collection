package com.keem.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserCatalog extends BaseEntity{

    @TableId(type = IdType.INPUT)
    private Long folderId;
    private Long parentId;
    private String folderName;
    private Integer userId;
    private Integer folderSno;
    private Integer folderLevel;
    private String folderPath;
}
