package com.keem.kochiu.collection.exception;

import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.ERROR_PARAM;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.SYS_ERROR;

/**
 * 统一异常处理类，用于处理校验异常、Feign异常和其他异常，同时提供统一的错误信息提取和返回。
 *
 * @author KoChiu
 * @date 2025/3/4
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = {Controller.class, RestController.class}, basePackages = "com.keem")
public class ExceptionControllerAdvice {

    /**
     * 处理校验异常，如BindException和MethodArgumentNotValidException。
     * 统一提取校验信息，并返回相应的错误结果。
     *
     * @param e 异常对象，可以是BindException或MethodArgumentNotValidException。
     * @return 返回包含校验错误信息的DefaultResult对象。
     */
    @Primary
    @ResponseBody
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public DefaultResult<String> methodArgumentNotValidExceptionHandler(Exception e) {
        BindingResult bindingResult = ((BindException) e).getBindingResult();

        List<String> msg = new ArrayList<>();
        if (bindingResult.getErrorCount() > 0) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            for (ObjectError error : errors) {
                msg.add(error.getDefaultMessage());
            }
        } else {
            msg = Collections.singletonList(ERROR_PARAM.getMessage());
        }
        return DefaultResult.buildError(ERROR_PARAM.getCode(), StringUtils.join(msg, ";"));
    }

    /**
     * 统一处理SaaS系统异常，如CloudException。
     *
     * @param e 异常对象，应为CloudException类型。
     * @return 返回包含系统异常信息的DefaultResult对象。
     */
    @ExceptionHandler(value = CollectionException.class)
    @ResponseBody
    public DefaultResult<String> erpExceptionHandler(CollectionException e) {

        try {
            //如果异常中有新异常信息则取新的，否则取错误码里的
            return DefaultResult.buildError(e.getErrorCode());
        } catch (Exception ex) {
            log.error("系统异常", e);
            return DefaultResult.buildError(SYS_ERROR);
        }
    }

    /**
     * 统一处理其他未被特定处理的异常。
     *
     * @param e 异常对象。
     * @return 返回包含通用错误信息的DefaultResult对象。
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public DefaultResult<String> exceptionHandler(Exception e) {

        if (e.getCause() instanceof CollectionException) {
            return erpExceptionHandler((CollectionException) e.getCause());
        }

        log.error("系统异常", e);
        return DefaultResult.buildError(SYS_ERROR);
    }
}
