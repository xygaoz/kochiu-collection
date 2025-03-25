package com.keem.kochiu.collection.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keem.kochiu.collection.entity.SysSecurity;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.mapper.SecurityMapper;
import com.keem.kochiu.collection.mapper.UserMapper;
import com.keem.kochiu.collection.util.RsaHexUtil;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class SecurityRepository extends ServiceImpl<SecurityMapper, SysSecurity>{

    /**
     * 获取系统公钥
     * @return
     * @throws CollectionException
     */
    public PublicKey getPublicKey() throws CollectionException {
        try {
            SysSecurity sysSecurity = getOne(null);
            if(sysSecurity == null){
                throw new CollectionException("获取公钥失败");
            }
            return RsaHexUtil.getPublicKey(sysSecurity.getPublicKey(), "10001");
        }
        catch (Exception e){
            throw new CollectionException("获取公钥失败");
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
                throw new CollectionException("获取私钥失败");
            }
            return RsaHexUtil.getPrivateKey(sysSecurity.getPrivateKey());
        }
        catch (Exception e){
            throw new CollectionException("获取私钥失败");
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
                throw new CollectionException("获取公用加密key失败");
            }
            return sysSecurity.getCommonKey();
        }
        catch (Exception e){
            throw new CollectionException("获取公用加密key失败");
        }
    }
}
