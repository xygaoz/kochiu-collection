package com.keem.kochiu.collection.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 接口统一返回结果类。用于封装接口的返回数据，包括成功与否、数据模型、状态码、错误码等信息。
 * @author KoChiu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Data
public class DefaultResult<T> implements Serializable {

    /**
     * 返回的数据模型
     */
    private T model;
    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 状态码或错误码
     */
    private String code = "0000";
    /**
     * 描述信息
     */
    private String message;

    /**
     * 默认构造函数，初始化为成功状态
     */
    public DefaultResult(){
        this.success = true;
    }

    /**
     * 带数据的构造函数，初始化为成功状态
     */
    public DefaultResult(T model){
        this.success = true;
        this.model = model;
    }

    /**
     * 带数据和消息的构造函数，初始化为成功状态
     */
    public DefaultResult(T model, String message){
        this.success = true;
        this.model = model;
        this.message = message;
    }

    /**
     * 带数据和消息的构造函数，初始化为成功状态
     */
    public DefaultResult(String code, T model, String message){
        this.code = code;
        this.success = true;
        this.model = model;
        this.message = message;
    }

    /**
     * 创建一个操作成功的返回结果
     */
    public static <T> DefaultResult<T> ok(){
        return new DefaultResult<>();
    }

    /**
     * 创建一个带数据的成功返回结果
     */
    public static <T> DefaultResult<T> ok(T model){
        return new DefaultResult<T>(model);
    }

    /**
     * 创建一个带数据和消息的成功返回结果
     */
    public static <T> DefaultResult<T> ok(T model, String message){
        return new DefaultResult<T>(model, message);
    }

    /**
     * 创建一个操作失败的返回结果
     */
    public static <T> DefaultResult<T> fail(){
        return new DefaultResult<>(false);
    }

    /**
     * 创建一个带消息的失败返回结果
     */
    public static <T> DefaultResult<T> fail(String message){
        return new DefaultResult<>(false, message);
    }

    /**
     * 带成功状态的构造函数
     */
    public DefaultResult(boolean success){
        this.success = success;
    }

    /**
     * 带成功状态和消息的构造函数
     */
    public DefaultResult(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    /**
     * 创建带自定义消息的系统错误返回结果
     */
    public static DefaultResult<String> buildError(String code, String defaultMessage){
        DefaultResult<String> result = new DefaultResult<>(code , null, defaultMessage);
        result.setSuccess(false);
        return result;
    }

    /**
     * 设置返回消息
     */
    public DefaultResult<T> setMessage(String message){
        this.message = message;
        return this;
    }

}
