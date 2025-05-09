package com.kochiu.collection.data.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchUserBo extends PageBo{

    private String userCode;
    private String userName;
}
