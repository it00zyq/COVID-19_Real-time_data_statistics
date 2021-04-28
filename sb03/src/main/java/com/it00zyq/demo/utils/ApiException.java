package com.it00zyq.demo.utils;

import lombok.Getter;

/**
 * 自定义异常类
 * @author IT00ZYQ
 * @date 2021/4/9 19:31
 **/
@Getter
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public ApiException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(String str){
        super(str);
        this.code = 500;
    }

}