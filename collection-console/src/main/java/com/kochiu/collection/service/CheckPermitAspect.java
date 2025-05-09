package com.kochiu.collection.service;

import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.annotation.Module;
import com.kochiu.collection.data.dto.TokenDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.enums.PermitEnum;
import com.kochiu.collection.exception.CollectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static com.kochiu.collection.Constant.*;
import static com.kochiu.collection.enums.ErrorCodeEnum.ERROR_TOKEN_INVALID;
import static com.kochiu.collection.enums.ErrorCodeEnum.PERMISSION_IS_INVALID;
import static com.kochiu.collection.enums.PermitEnum.API;


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
    private final SysSecurityService securityService;

    @Value("${server.servlet.context-path}")
    private String contextPath = "";

    public CheckPermitAspect(TokenService tokenService,
                             SysSecurityService securityService) {
        this.tokenService = tokenService;
        this.securityService = securityService;
    }

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(com.kochiu.collection.annotation.CheckPermit)")
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

            /// 获取当前访问的 URL
            String requestUrl = request.getRequestURI();
            requestUrl = requestUrl.substring(request.getContextPath().length());
            log.info("Current request URL: {}", requestUrl);

            String authorization = request.getHeader(HEADER_AUTHORIZATION);
            if(!checkPermit(requestUrl, authorization, checkPermit)){
                throw new CollectionException(ERROR_TOKEN_INVALID);
            }
        }
    }

    private boolean checkPermit(String requestUrl, String authorization, CheckPermit checkPermit) throws CollectionException {

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

        //校验权限
        if(checkPermit.modules() != null && checkPermit.modules().length > 0) {
            boolean isPermit = false;
            for(Module module : checkPermit.modules()){
                if(module.byAction() == null){
                    if(securityService.hasPermission(tokenDto.getUser().getUserId(), module.modeCode(), null)){
                        isPermit = true;
                    }
                }
                else{
                    for(String action : module.byAction()){
                        if(securityService.hasPermission(tokenDto.getUser().getUserId(), module.modeCode(), action)){
                            if(securityService.urlMatching(requestUrl, module.modeCode(), action)){
                                isPermit = true;
                            }
                        }
                    }
                }
            }
            if(!isPermit){
                throw new CollectionException(PERMISSION_IS_INVALID);
            }
        }

        USER_INFO.set(UserDto.builder()
                        .userCode(tokenDto.getUser().getUserCode())
                        .userId(tokenDto.getUser().getUserId())
                .build());
        return true;
    }
}
