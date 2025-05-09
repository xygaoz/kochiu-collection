package com.kochiu.collection.exception;

import com.kochiu.collection.enums.ErrorCodeEnum;
import lombok.Getter;

@Getter
public class CollectionException extends Exception{

    private ErrorCodeEnum errorCode;

    public CollectionException(String message){
        super(message);
    }

    public CollectionException(ErrorCodeEnum code){
        super(code.getMessage());
        this.errorCode = code;
    }

    public CollectionException(ErrorCodeEnum errorCodeEnum, String message) {
        super(message);
        this.errorCode = errorCodeEnum;
    }
}
