package com.keem.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginDto {
    private String username;
    private String token;
    private int expirySeconds;
}
