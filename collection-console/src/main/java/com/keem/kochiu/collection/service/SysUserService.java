package com.keem.kochiu.collection.service;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.LoginBo;
import com.keem.kochiu.collection.data.dto.LoginDto;
import com.keem.kochiu.collection.data.dto.TokenDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.util.HexUtils;
import com.keem.kochiu.collection.util.RsaHexUtil;
import com.keem.kochiu.collection.util.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

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
    public TokenDto genToken(LoginBo loginBo) throws CollectionException {
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
        TokenDto tokenDto = genToken(loginBo, PermitEnum.UI, 30 * 60 * 1000);
        String refreshToken = tokenService.createToken(tokenDto.getUser(), tokenDto.getClaims(), 7 * 24 * 3600 * 1000);
        return LoginDto.builder()
                .username(loginBo.getUsername())
                .token(tokenDto.getToken())
                .refreshToken(refreshToken)
                .expirySeconds(30 * 60)
                .build();
    }

    /**
     * 根据登录用户信息生成token
     * @param loginBo
     * @return
     */
    private TokenDto genToken(LoginBo loginBo, PermitEnum permitEnum, long ttlMillis) throws CollectionException {

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUserCode, loginBo.getUsername());
        try {
            SysUser user = userRepository.getOne(lambdaQueryWrapper);
            if(user != null){
                String loginPwd = RsaHexUtil.decrypt(loginBo.getPassword(), securityRepository.getPrivateKey());
                loginPwd = SHA256Util.encryptBySHA256(loginPwd);
                if(loginPwd.equals(user.getPassword())){
                    Map<String, Object> claims = Map.of(
                            TOKEN_API_FLAG, permitEnum.name(),
                            TOKEN_TYPE_FLAG, TOKEN_TYPE_ACCESS
                    );
                    String token = tokenService.createToken(user,
                            claims,
                            ttlMillis);

                    if(ttlMillis == -1) {
                        //api访问时，token保存到数据库
                        user.setToken(token);
                    }
                    user.setUpdateBy(loginBo.getUsername());
                    userRepository.updateById(user);
                    return TokenDto.builder()
                            .user(user)
                            .token(token)
                            .claims(claims)
                            .build();
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

    public void updateLastRefreshTime(SysUser user) {
        user.setLastTokenTime(LocalDateTime.now());
        userRepository.updateById(user);
    }
}
