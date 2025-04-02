package com.keem.kochiu.collection.service;

import com.keem.kochiu.collection.annotation.CheckPermit;
import com.keem.kochiu.collection.data.dto.TokenDto;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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

import static com.keem.kochiu.collection.Constant.*;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.ERROR_TOKEN_INVALID;
import static com.keem.kochiu.collection.enums.PermitEnum.API;


/**
 * 权限注解拦截器
 * @author KoChiu
 */
@Slf4j
@Aspect //该注解标示该类为切面类
@Component //注入依赖
public class CheckPermitAspect {

    public static ThreadLocal<UserDto> USER_INFO = new ThreadLocal<>();
    private final TokenService tokenService;

    public CheckPermitAspect(TokenService tokenService) {
        this.tokenService = tokenService;
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

        TokenDto tokenDto = tokenService.validateToken(authorization,
                ArrayUtils.contains(checkPermit.on(), API) && checkPermit.on().length == 1);
        if(!TOKEN_TYPE_ACCESS.equals(tokenDto.getClaims().get(TOKEN_TYPE_FLAG))){
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }

        boolean found = false;
        for(PermitEnum permit : checkPermit.on()){
            if (permit.name().equals(tokenDto.getClaims().get(TOKEN_API_FLAG))){
                found =  true;
            }
        }
        if(!found){
            throw new CollectionException(ERROR_TOKEN_INVALID);
        }

        if(tokenDto.getClaims().get(TOKEN_API_FLAG).equals(API.name())){
            if(!tokenDto.getUser().getToken().equals(authorization)){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }

        USER_INFO.set(UserDto.builder()
                        .userCode(tokenDto.getUser().getUserCode())
                        .userId(tokenDto.getUser().getUserId())
                .build());
        return true;
    }
}
