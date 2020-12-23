package com.jianli.common.exception;


/**
 *   未知服务器内部错误异常类
 */
public class InternalServerException extends  BaseException {
    public InternalServerException(String message)
    {
        super(500, message);
    }
}
