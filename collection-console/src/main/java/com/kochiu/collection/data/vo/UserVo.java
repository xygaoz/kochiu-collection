package com.kochiu.collection.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class UserVo {

    private Integer userId;
    private String userCode;
    private String userName;
    private String token;
    private String key;
    private String strategy;
    private Integer status;
    private int canDel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private List<RoleVo> roles;
}
