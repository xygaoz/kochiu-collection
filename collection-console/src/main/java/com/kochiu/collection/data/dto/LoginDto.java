package com.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class LoginDto {

    private int userId;
    private String userCode;
    private String userName;
    private String token;
    private String refreshToken;
    private int expirySeconds;
    private Set<String> permissions;
    private int canDel;
}
