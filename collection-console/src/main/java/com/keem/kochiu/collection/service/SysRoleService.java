package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.entity.SysRole;
import com.keem.kochiu.collection.repository.SysRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleService {

    private final SysRoleRepository sysRoleRepository;

    public SysRoleService(SysRoleRepository sysRoleRepository) {
        this.sysRoleRepository = sysRoleRepository;
    }

    public List<SysRole> selectUserRole(int userId){
        return sysRoleRepository.selectUserRole(userId);
    }
}
