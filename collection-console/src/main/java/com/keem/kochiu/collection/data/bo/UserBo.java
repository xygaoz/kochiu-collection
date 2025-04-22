package com.keem.kochiu.collection.data.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserBo extends PageBo{

    private String userCode;
    private String userName;
}
