package com.jianli.common.exception;

/**
 *   请求数据格式错误异常类
 */
public class BadRequestException extends  BaseException {
    public BadRequestException(String message)
    {
        super(400, message);
    }
}
