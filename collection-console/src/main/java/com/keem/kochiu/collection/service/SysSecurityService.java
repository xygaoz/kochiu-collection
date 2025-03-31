package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import org.springframework.stereotype.Service;

@Service
public class SysSecurityService {

    private final SysSecurityRepository securityRepository;

    public SysSecurityService(SysSecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    public String getPublicKey() throws CollectionException {
        return securityRepository.getPublicKeyStr();
    }
}
