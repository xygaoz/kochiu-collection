package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.bo.RoleBo;
import com.kochiu.collection.data.vo.RoleVo;
import com.kochiu.collection.entity.SysModuleAction;
import com.kochiu.collection.entity.SysRole;
import com.kochiu.collection.entity.UserPermission;
import com.kochiu.collection.entity.UserRole;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.YesNoEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysModuleActionRepository;
import com.kochiu.collection.repository.SysRoleRepository;
import com.kochiu.collection.repository.UserPermissionRepository;
import com.kochiu.collection.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SysRoleService {

    private final SysRoleRepository sysRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final SysModuleActionRepository sysModuleActionRepository;
    private final UserRoleRepository userRoleRepository;

    public SysRoleService(SysRoleRepository sysRoleRepository,
                          UserPermissionRepository userPermissionRepository,
                          SysModuleActionRepository sysModuleActionRepository,
                          UserRoleRepository userRoleRepository) {
        this.sysRoleRepository = sysRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.sysModuleActionRepository = sysModuleActionRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // 根据用户ID查询角色列表
    public List<SysRole> selectUserRole(int userId){
        return sysRoleRepository.selectUserRole(userId);
    }

    // 查询所有角色
    public List<RoleVo> selectAll() {
        return sysRoleRepository.list().stream().map(role -> {
            List<UserPermission> permissions = userPermissionRepository.getRolePermission(role.getRoleId());
            return RoleVo.builder()
                    .roleId(role.getRoleId())
                    .roleName(role.getRoleName())
                    .canDel(role.getCanDel())
                    .permissions(permissions.stream().map(permission -> RoleVo.Permission.builder()
                                    .actionId(permission.getActionId())
                                    .moduleName(permission.getModuleName())
                                    .actionName(permission.getActionName())
                                    .build())
                                    .collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());
    }

    // 添加角色
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleBo roleBo){

        SysRole role = SysRole.builder()
                .roleName(roleBo.getRoleName())
                .build();
        sysRoleRepository.save(role);
        Long roleId = sysRoleRepository.getBaseMapper().selectLastInsertId();
        // 添加权限
        if(roleBo.getPermissions() != null) {
            for (Long actionId : roleBo.getPermissions()) {

                SysModuleAction action = sysModuleActionRepository.getById(actionId);

                UserPermission permission = UserPermission.builder()
                        .roleId(roleId)
                        .moduleId(action.getModuleId())
                        .actionId(actionId)
                        .build();
                userPermissionRepository.save(permission);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleBo roleBo) throws CollectionException {

        SysRole role = sysRoleRepository.getById(roleBo.getRoleId());
        if(role == null){
            throw new CollectionException(ErrorCodeEnum.ROLE_IS_NOT_EXIST);
        }
        role.setRoleName(roleBo.getRoleName());
        sysRoleRepository.updateById(role);

        //非超级管理员角色才能改权限
        if(YesNoEnum.getEnum(role.getCanDel()) == YesNoEnum.YES) {
            //先删除权限再添加
            userPermissionRepository.remove(new LambdaQueryWrapper<UserPermission>()
                    .eq(UserPermission::getRoleId, roleBo.getRoleId()));
            if(roleBo.getPermissions() != null) {
                for (Long actionId : roleBo.getPermissions()) {

                    SysModuleAction action = sysModuleActionRepository.getById(actionId);

                    UserPermission permission = UserPermission.builder()
                            .roleId(role.getRoleId())
                            .moduleId(action.getModuleId())
                            .actionId(actionId)
                            .build();
                    userPermissionRepository.save(permission);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) throws CollectionException {
        SysRole role = sysRoleRepository.getById(roleId);
        if(role == null){
            throw new CollectionException(ErrorCodeEnum.ROLE_IS_NOT_EXIST);
        }
        if(YesNoEnum.getEnum(role.getCanDel()) == YesNoEnum.NO){
            throw new CollectionException(ErrorCodeEnum.ROLE_IS_NOT_DELETE);
        }
        sysRoleRepository.removeById(roleId);

        //删除权限
        userPermissionRepository.remove(new LambdaQueryWrapper<UserPermission>()
                .eq(UserPermission::getRoleId, roleId));
        //删除用户角色
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
    }
}
