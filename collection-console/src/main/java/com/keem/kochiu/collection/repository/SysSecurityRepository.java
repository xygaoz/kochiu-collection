package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysSecurity;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.SysSecurityMapper;
import com.keem.kochiu.collection.util.RsaHexUtil;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;

@Service
public class SysSecurityRepository extends ServiceImpl<SysSecurityMapper, SysSecurity>{

    /**
     * 获取系统公钥
     * @return
     * @throws CollectionException
     */
    public PublicKey getPublicKey() throws CollectionException {
        try {
            SysSecurity sysSecurity = getOne(null);
            if(sysSecurity == null){
                throw new CollectionException(FAILED_GET_PUBLIC_KEY);
            }
            return RsaHexUtil.getPublicKey(sysSecurity.getPublicKey(), "10001");
        }
        catch (Exception e){
            throw new CollectionException(FAILED_GET_PUBLIC_KEY);
        }
    }

    /**
     * 获取系统公钥
     * @return
     * @throws CollectionException
     */
    public String getPublicKeyStr() throws CollectionException {
        try {
            SysSecurity sysSecurity = getOne(null);
            if(sysSecurity == null){
                throw new CollectionException(FAILED_GET_PUBLIC_KEY);
            }
            PublicKey pub = RsaHexUtil.getPublicKey(sysSecurity.getPublicKey(), "10001");
            return RsaHexUtil.publicKeyToPem(pub);
        }
        catch (Exception e){
            throw new CollectionException(FAILED_GET_PUBLIC_KEY);
        }
    }

    /**
     * 获取系统私钥
     * @return
     * @throws CollectionException
     */
    public PrivateKey getPrivateKey() throws CollectionException {
        try {
            SysSecurity sysSecurity = getOne(null);
            if(sysSecurity == null){
                throw new CollectionException(FAILED_GET_PRIVATE_KEY);
            }
            return RsaHexUtil.getPrivateKey(sysSecurity.getPrivateKey());
        }
        catch (Exception e){
            throw new CollectionException(FAILED_GET_PRIVATE_KEY);
        }
    }

    /**
     * 获取公用加密key
     * @return
     * @throws CollectionException
     */
    public String getCommonKey() throws CollectionException {
        try {
            SysSecurity sysSecurity = getOne(null);
            if(sysSecurity == null){
                throw new CollectionException(FAILED_GET_COMMON_KEY);
            }
            return sysSecurity.getCommonKey();
        }
        catch (Exception e){
            throw new CollectionException(FAILED_GET_COMMON_KEY);
        }
    }
}
