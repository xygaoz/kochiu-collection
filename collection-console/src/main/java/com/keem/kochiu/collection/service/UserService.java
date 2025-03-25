package com.keem.kochiu.collection.service;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.LoginBo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SecurityRepository;
import com.keem.kochiu.collection.repository.UserRepository;
import com.keem.kochiu.collection.util.JwtUtil;
import com.keem.kochiu.collection.util.RsaHexUtil;
import com.keem.kochiu.collection.util.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;

    public UserService(UserRepository userRepository, SecurityRepository securityRepository) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
    }

    /**
     * 根据登录用户信息生成token
     * @param loginBo
     * @return
     */
    public String genToken(LoginBo loginBo) throws CollectionException {

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUserCode, loginBo.getUsername());
        try {
            SysUser user = userRepository.getOne(lambdaQueryWrapper);
            if(user != null){
                String loginPwd = RsaHexUtil.decrypt(loginBo.getPassword(), securityRepository.getPrivateKey());
                loginPwd = SHA256Util.encryptBySHA256(loginPwd);
                if(loginPwd.equals(user.getPassword())){
                    String token;
                    try {
                        token = JwtUtil.createJWT(MapUtil.builder("api", "true").map(), user.getKey(),
                                RandomStringUtils.randomNumeric(8), String.valueOf(user.getUserId()), 0);
                    }
                    catch (Exception e) {
                        log.error("生成token失败", e);
                        throw new CollectionException("生成token失败");
                    }
                    user.setToken(token);
                    userRepository.updateById(user);
                    return token;
                }
                else{
                    throw new CollectionException("无效的用户名或密码");
                }
            }
            else{
                throw new CollectionException("无效的用户名或密码");
            }
        }
        catch (CollectionException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("系统错误", e);
            throw new CollectionException("系统错误");
        }
    }
}
