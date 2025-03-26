package com.keem.kochiu.collection.service;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.LoginBo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SecurityRepository;
import com.keem.kochiu.collection.repository.UserRepository;
import com.keem.kochiu.collection.util.AesUtil;
import com.keem.kochiu.collection.util.JwtUtil;
import com.keem.kochiu.collection.util.RsaHexUtil;
import com.keem.kochiu.collection.util.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import static com.keem.kochiu.collection.Constant.TOKEN_API_FLAG;

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
                        token = JwtUtil.createJWT(MapUtil.builder(TOKEN_API_FLAG, PermitEnum.API.name()).map(), user.getKey(),
                                RandomStringUtils.randomNumeric(8), String.valueOf(user.getUserId()), -1);
                        //拼接加密的userid 和 token
                        token = AesUtil.aesEncrypt(String.valueOf(user.getUserId()), securityRepository.getCommonKey()) + "." + token;
                    }
                    catch (Exception e) {
                        log.error("生成token失败", e);
                        throw new CollectionException("生成token失败");
                    }
                    user.setToken(token);
                    user.setUpdateBy(loginBo.getUsername());
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
