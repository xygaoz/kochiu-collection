package com.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class RoleVo {

    private Long roleId;
    private String roleName;
    private int canDel;
    private List<Permission> permissions;

    @Builder
    @Data
    public static class Permission{
        private Long actionId;
        private String moduleName;
        private String actionName;
    }
}
