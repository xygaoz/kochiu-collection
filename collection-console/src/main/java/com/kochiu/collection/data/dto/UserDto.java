package com.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    private int userId;
    private String userCode;
}
