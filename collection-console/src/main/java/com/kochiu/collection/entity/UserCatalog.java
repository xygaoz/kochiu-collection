package com.kochiu.collection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode(callSuper = true)
@Data
public class UserCatalog extends BaseEntity{

    @TableId(type = IdType.INPUT)
    private Long cataId;
    private Long parentId;
    private String cataName;
    private Integer userId;
    private Integer cataSno;
    private Integer cataLevel;
    private String cataPath;
}
