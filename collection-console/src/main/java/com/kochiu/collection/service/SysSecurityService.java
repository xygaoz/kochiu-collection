package com.kochiu.collection.service;

import com.kochiu.collection.entity.SysModuleAction;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysModuleActionRepository;
import com.kochiu.collection.repository.SysSecurityRepository;
import com.kochiu.collection.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;

@Service
public class SysSecurityService {

    private final SysSecurityRepository securityRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final SysModuleActionRepository moduleActionRepository;

    public SysSecurityService(SysSecurityRepository securityRepository,
                              UserPermissionRepository userPermissionRepository,
                              SysModuleActionRepository moduleActionRepository) {
        this.securityRepository = securityRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.moduleActionRepository = moduleActionRepository;
    }

    public String getPublicKey() throws CollectionException {
        return securityRepository.getPublicKeyStr();
    }

    public boolean hasPermission(int userId, String moduleCode, String actionCode){
        return userPermissionRepository.hasPermission(userId, moduleCode, actionCode);
    }

    public boolean urlMatching(String url, String moduleCode, String actionCode) {

        SysModuleAction action = moduleActionRepository.selectModuleAction(moduleCode, actionCode);
        return url.startsWith(action.getActionUrl());
    }
}
