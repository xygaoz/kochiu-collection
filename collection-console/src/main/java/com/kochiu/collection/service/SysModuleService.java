package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.ActionVo;
import com.kochiu.collection.data.vo.ModuleVo;
import com.kochiu.collection.entity.SysModule;
import com.kochiu.collection.entity.SysModuleAction;
import com.kochiu.collection.entity.UserRole;
import com.kochiu.collection.repository.SysModuleActionRepository;
import com.kochiu.collection.repository.SysModuleRepository;
import com.kochiu.collection.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysModuleService {

    private final SysModuleRepository moduleRepository;
    private final SysModuleActionRepository actionRepository;
    private final UserRoleRepository userRoleRepository;

    public SysModuleService(SysModuleRepository moduleRepository,
                            SysModuleActionRepository actionRepository,
                            UserRoleRepository userRoleRepository) {

        this.moduleRepository = moduleRepository;
        this.actionRepository = actionRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * 获取用户下拥有的权限模块列表
     */
    public Set<ModuleVo> getPermitModuleList(UserDto userDto) {
        return getPermitModuleList(userDto.getUserId(), false);
    }

    public Set<ModuleVo> getPermitModuleList(int userId, boolean onlySelected) {
        // 1. 获取用户角色
        List<UserRole> roles = userRoleRepository.list(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );

        // 2. 获取所有顶级模块(parentId=0)
        List<SysModule> topLevelModules = moduleRepository.list(
                new LambdaQueryWrapper<SysModule>().eq(SysModule::getParentId, 0)
        );

        Set<ModuleVo> result = new HashSet<>();

        // 3. 递归处理每个顶级模块及其子模块
        for (SysModule topModule : topLevelModules) {
            ModuleVo moduleVo = buildModuleTree(topModule, roles, onlySelected);
            if (moduleVo != null) {
                result.add(moduleVo);
            }
        }

        return result;
    }

    /**
     * 递归构建模块树
     */
    private ModuleVo buildModuleTree(SysModule module, List<UserRole> roles, boolean onlySelected) {
        // 1. 获取当前模块的所有子模块
        List<SysModule> subModules = moduleRepository.list(
                new LambdaQueryWrapper<SysModule>().eq(SysModule::getParentId, module.getModuleId())
        );

        // 2. 构建ModuleVo对象
        ModuleVo moduleVo = ModuleVo.builder()
                .moduleId(module.getModuleId())
                .moduleName(module.getModuleName())
                .moduleCode(module.getModuleCode())
                .build();

        if (!subModules.isEmpty()) {
            // 3. 如果有子模块，递归处理子模块
            List<ModuleVo> children = new ArrayList<>();
            for (SysModule subModule : subModules) {
                ModuleVo child = buildModuleTree(subModule, roles, onlySelected);
                if (child != null) {
                    children.add(child);
                }
            }
            moduleVo.setChildren(children);
        } else {
            // 4. 如果没有子模块，获取该模块的动作权限
            List<ActionVo> actionVos = new ArrayList<>();

            // 遍历用户的所有角色，获取每个角色在该模块下的权限
            for (UserRole role : roles) {
                List<SysModuleAction> actions = actionRepository.getRolePermission(
                        role.getRoleId(),
                        module.getModuleId()
                );

                for (SysModuleAction action : actions) {
                    if(onlySelected){
                        if(action.isSelected()) {
                            actionVos.add(ActionVo.builder()
                                    .actionId(action.getActionId())
                                    .actionName(action.getActionName())
                                    .actionCode(action.getActionCode())
                                    .selected(action.isSelected())
                                    .build()
                            );
                        }
                    }
                    else{
                        actionVos.add(ActionVo.builder()
                                .actionId(action.getActionId())
                                .actionName(action.getActionName())
                                .actionCode(action.getActionCode())
                                .selected(action.isSelected())
                                .build()
                        );
                    }
                }
            }

            // 去重
            if (!actionVos.isEmpty()) {
                moduleVo.setActions(actionVos.stream()
                        .distinct()
                        .collect(Collectors.toList()));
            }
        }

        // 5. 只有包含子模块或动作的模块才返回
        return (moduleVo.getChildren() != null || moduleVo.getActions() != null) ? moduleVo : null;
    }
}
