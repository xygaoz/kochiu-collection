package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.util.AesUtil;
import com.keem.kochiu.collection.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

import static com.keem.kochiu.collection.Constant.*;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.*;
import static com.keem.kochiu.collection.enums.PermitEnum.*;


/**
 * 权限注解拦截器
 * @author KoChiu
 */
@Slf4j
@Aspect //该注解标示该类为切面类
@Component //注入依赖
public class CheckPermitAspect {

    public static ThreadLocal<UserDto> USER_INFO = new ThreadLocal<>();
    private final SysSecurityRepository securityRepository;
    private final SysUserRepository userRepository;

    public CheckPermitAspect(SysSecurityRepository securityRepository, SysUserRepository userRepository) {
        this.securityRepository = securityRepository;
        this.userRepository = userRepository;
    }

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(com.keem.kochiu.collection.annotation.CheckPermit)")
    public void checkPointCut() {
    }

    /**
     * 前置通知 用于拦截操作，在方法返回后执行
     * @param joinPoint 切点
     */
    @Before(value = "checkPointCut()")
    public void doBefore(JoinPoint joinPoint) throws Exception {

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        CheckPermit checkPermit = method.getAnnotation(CheckPermit.class);

        if(checkPermit != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String authorization = request.getHeader(HEADER_AUTHORIZATION);
            if(!checkPermit(authorization, checkPermit)){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }
    }

    private boolean checkPermit(String authorization, CheckPermit checkPermit) throws CollectionException {
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

        if (checkPermit.on() != ALL) {
            if (!checkPermit.on().name().equals(data.get(TOKEN_API_FLAG))){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }

        if(data.get(TOKEN_API_FLAG).equals(API.name())){
            if(!user.getToken().equals(authorization)){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }

        USER_INFO.set(UserDto.builder()
                        .userCode(user.getUserCode())
                        .userId(user.getUserId())
                .build());
        return true;
    }
}
