package com.keem.kochiu.collection.data.dto;

import com.keem.kochiu.collection.entity.SysUser;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class TokenDto {

    private SysUser user;
    private Map<String, Object> claims;
}
