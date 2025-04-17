package com.keem.kochiu.collection.data.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterResourceBo extends PageBo{

    private Long cateId;
    private String keyword;
    private String[] types;
    private String[] tags;
    private Integer cataSno;
}
