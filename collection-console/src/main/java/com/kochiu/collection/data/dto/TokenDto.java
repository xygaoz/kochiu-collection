package com.kochiu.collection.data.dto;

import com.kochiu.collection.entity.SysUser;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class TokenDto {

    private SysUser user;
    private String token;
    private Map<String, Object> claims;
}
