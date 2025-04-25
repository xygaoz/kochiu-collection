package com.keem.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginDto {
    private String userCode;
    private String userName;
    private String token;
    private String refreshToken;
    private int expirySeconds;
}
