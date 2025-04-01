package com.keem.kochiu.collection.service;

import cn.hutool.core.map.MapUtil;
import com.keem.kochiu.collection.data.dto.TokenDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.util.AesUtil;
import com.keem.kochiu.collection.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.keem.kochiu.collection.Constant.TOKEN_API_FLAG;
import static com.keem.kochiu.collection.Constant.TOKEN_PARAMS_FLAG;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.ERROR_TOKEN_INVALID;

@Slf4j
@Service
public class TokenService {

    private final SysSecurityRepository securityRepository;
    private final SysUserRepository userRepository;

    public TokenService(SysSecurityRepository securityRepository, SysUserRepository userRepository) {
        this.securityRepository = securityRepository;
        this.userRepository = userRepository;
    }

    /**
     * 生成token
     * @param user
     * @param params
     * @param ttlMillis
     * @return
     * @throws CollectionException
     */
    public String createToken(SysUser user, Object params, long ttlMillis) throws CollectionException {
        String token;
        try {
            token = JwtUtil.createJWT(params, user.getKey(),
                    RandomStringUtils.randomNumeric(8), String.valueOf(user.getUserId()), ttlMillis);
            //拼接加密的userid 和 token
            token = AesUtil.aesEncrypt(String.valueOf(user.getUserId()), securityRepository.getCommonKey()) + "." + token;
            return token;
        }
        catch (Exception e) {
            log.error("生成token失败", e);
            throw new CollectionException("生成token失败");
        }
    }

    /**
     * 验证token
     * @param authorization
     * @return
     * @throws CollectionException
     */
    public TokenDto validateToken(String authorization) throws CollectionException {

        if(authorization == null){
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }
        if(!authorization.contains(".")){
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }

        //检验token
        String userId = authorization.substring(0, authorization.indexOf("."));
        String token = authorization.substring(authorization.indexOf(".") + 1);

        //解密userId
        try {
            userId = AesUtil.aesDecrypt(userId, securityRepository.getCommonKey());
        }
        catch (Exception e){
            log.error("解密userId失败", e);
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }
        //获取用户
        SysUser user = userRepository.getById(userId);
        if (user == null) {
            log.error("用户不存在");
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }
        if(user.getToken() == null){
            throw new CollectionException(ERROR_TOKEN_NOT_EXIST);
        }

        //解密token
        Map<String, Object> data;
        try {
            Claims claims = JwtUtil.parseJWT(token, user.getKey());
            data = (Map<String, Object>) claims.get(TOKEN_PARAMS_FLAG);
            if(!claims.getSubject().equals(userId)){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }
        catch (ExpiredJwtException e) {
            throw new CollectionException(ERROR_TOKEN_EXPIRE);
        }
        catch (CollectionException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("解密token失败", e);
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }

        if(data == null || !data.containsKey(TOKEN_API_FLAG)){
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }

        return TokenDto.builder()
                .claims(data)
                .user(user)
                .build();
    }
}
