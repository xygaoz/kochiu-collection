package com.kochiu.collection.controller;

import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.LoginBo;
import com.kochiu.collection.data.dto.LoginDto;
import com.kochiu.collection.data.dto.TokenDto;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.service.SysSecurityService;
import com.kochiu.collection.service.SysUserService;
import com.kochiu.collection.service.TokenService;
import com.kochiu.collection.util.IpUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.ZoneId;
import java.util.Map;

import static com.kochiu.collection.Constant.*;

@Controller
@RequestMapping(PUBLIC_URL + "/sys")
public class SysSecurityController {

    private final SysUserService userService;
    private final SysSecurityService securityService;
    private final TokenService tokenService;

    public SysSecurityController(SysUserService userService,
                                 SysSecurityService securityService,
                                 TokenService tokenService) {
        this.userService = userService;
        this.securityService = securityService;
        this.tokenService = tokenService;
    }

    /**
     * 生成api token
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    @ResponseBody
    @PostMapping("/tokens")
    public DefaultResult<String> tokens(@Valid LoginBo loginBo) throws CollectionException {

        return DefaultResult.ok(userService.genToken(loginBo).getToken());
    }

    /**
     * 获取公钥
     * @return
     * @throws CollectionException
     */
    @ResponseBody
    @GetMapping("/publicKey")
    public DefaultResult<String> getPublicKey() throws CollectionException {
        return DefaultResult.ok(securityService.getPublicKey());
    }

    /**
     * 登录
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    @ResponseBody
    @PostMapping("/login")
    public DefaultResult<LoginDto> login(HttpServletRequest request, @Valid LoginBo loginBo) throws CollectionException {
        loginBo.setIp(IpUtils.getClientIp( request));
        return DefaultResult.ok(userService.login(loginBo));
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@CookieValue("refresh_token") String refreshToken) {

        try{
            // 1. 验证refreshToken有效性
            TokenDto tokenDto = tokenService.validateToken(refreshToken, false);

            // 2. 判断一小时内是否已经刷新过，如果已经刷新过，则返回401
            if(tokenDto.getUser().getLastTokenTime() != null) {
                long timestampMillis = tokenDto.getUser().getLastTokenTime().atZone(ZoneId.of("Asia/Shanghai"))
                        .toInstant()
                        .toEpochMilli();
                if (tokenDto.getUser().getLastTokenTime() != null
                        && timestampMillis + 1200 * 1000 > System.currentTimeMillis()) {
                    return ResponseEntity.status(401).build();
                }
            }

            // 3. 生成新accessToken, 有效期30分钟
            tokenDto.getClaims().put(TOKEN_TYPE_FLAG, TOKEN_TYPE_ACCESS);
            String newAccessToken = tokenService.createToken(tokenDto.getUser(), tokenDto.getClaims(), 30 * 60 * 1000);

            // 4. 生成新refreshToken（轮转机制）
            tokenDto.getClaims().put(TOKEN_TYPE_FLAG, TOKEN_TYPE_REFRESH);
            String newRefreshToken = tokenService.createToken(tokenDto.getUser(), tokenDto.getClaims(), 7 * 24 * 3600 * 1000);
            userService.updateLastRefreshTime(tokenDto.getUser());

            return ResponseEntity.ok()
                    .body(Map.of("token", newAccessToken, "refreshToken", newRefreshToken, "expirySeconds", 30 * 60));
        }
        catch (CollectionException e){
            return ResponseEntity.status(401).build();
        }
    }
}
