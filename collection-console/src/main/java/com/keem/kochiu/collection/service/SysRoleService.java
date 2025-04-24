package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.data.vo.RoleVo;
import com.keem.kochiu.collection.entity.SysRole;
import com.keem.kochiu.collection.entity.UserPermission;
import com.keem.kochiu.collection.repository.SysRoleRepository;
import com.keem.kochiu.collection.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final UserPermissionRepository userPermissionRepository;

    public SysRoleService(SysRoleRepository sysRoleRepository,
                          UserPermissionRepository userPermissionRepository) {
        this.sysRoleRepository = sysRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
    }

    public List<SysRole> selectUserRole(int userId){
        return sysRoleRepository.selectUserRole(userId);
    }

    public List<RoleVo> selectAll() {
        return sysRoleRepository.list().stream().map(role -> {
            List<UserPermission> permissions = userPermissionRepository.getRolePermission(role.getRoleId());
            return RoleVo.builder()
                    .roleId(role.getRoleId())
                    .roleName(role.getRoleName())
                    .permissions(permissions.stream().map(permission -> RoleVo.Permission.builder()
                                    .actionId(permission.getActionId())
                                    .moduleName(permission.getModuleName())
                                    .actionName(permission.getActionName())
                                    .build())
                                    .collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());
    }
}
