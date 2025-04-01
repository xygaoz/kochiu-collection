package com.keem.kochiu.collection.service;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.LoginBo;
import com.keem.kochiu.collection.data.dto.LoginDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import static com.keem.kochiu.collection.Constant.*;

@Slf4j
@Service
public class SysUserService {

    private final SysUserRepository userRepository;
    private final SysSecurityRepository securityRepository;
    private final TokenService tokenService;

    public SysUserService(SysUserRepository userRepository,
                          SysSecurityRepository securityRepository,
                          TokenService tokenService) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
        this.tokenService = tokenService;
    }

    /**
     * 根据登录用户信息生成token，用户api操作
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    public String genToken(LoginBo loginBo) throws CollectionException {
        return genToken(loginBo, PermitEnum.API, -1);
    }

    /**
     * 根据登录用户信息生成token，用户ui操作, 默认30分钟超时
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    public LoginDto login(LoginBo loginBo) throws CollectionException {
        //前端rsa加密是base64加密，所以解密时需要base64解密
        loginBo.setPassword(HexUtils.base64ToHex(loginBo.getPassword()));
        String token = genToken(loginBo, PermitEnum.UI, 30 * 60 * 1000);
        return LoginDto.builder()
                .username(loginBo.getUsername())
                .token(token)
                .expirySeconds(30 * 60)
                .build();
    }

    /**
     * 根据登录用户信息生成token
     * @param loginBo
     * @return
     */
    private String genToken(LoginBo loginBo, PermitEnum permitEnum, long ttlMillis) throws CollectionException {

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUserCode, loginBo.getUsername());
        try {
            SysUser user = userRepository.getOne(lambdaQueryWrapper);
            if(user != null){
                String loginPwd = RsaHexUtil.decrypt(loginBo.getPassword(), securityRepository.getPrivateKey());
                loginPwd = SHA256Util.encryptBySHA256(loginPwd);
                if(loginPwd.equals(user.getPassword())){
                    String token = tokenService.createToken(user,
                            MapUtil.of(
                                    Pair.of(TOKEN_API_FLAG, permitEnum.name()),
                                    Pair.of(TOKEN_TYPE_FLAG, TOKEN_TYPE_ACCESS)
                            ),
                            ttlMillis);

                    if(ttlMillis == -1) {
                        //api访问时，token保存到数据库
                        user.setToken(token);
                    }
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
