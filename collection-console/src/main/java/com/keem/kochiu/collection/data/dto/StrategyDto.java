package com.keem.kochiu.collection.data.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Builder
@Data
public class StrategyDto {

    private Integer strategyId;
    private String strategyCode;
    private String strategyName;

    @NotNull(message = "服务器地址不能为空")
    private String serverUrl;
    private String username;
    private String password;
    private String otherConfig;

}
