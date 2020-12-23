package com.jianli.common.exception;
/**
 *   身份验证异常类
 */
public class UnauthorizedException extends  BaseException {
    public UnauthorizedException(String message)
    {
        super(401, message);
    }
}
