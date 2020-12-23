package com.jianli.common.exception;


/**
 *   已知服务器内部错误异常类
 */
public class InternalKnownException extends  BaseException {
    public InternalKnownException(String message)
    {
        super(501, message);
    }
}
