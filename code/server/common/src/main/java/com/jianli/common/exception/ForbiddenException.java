package com.jianli.common.exception;

/**
 *   授权验证异常类
 */
public class ForbiddenException extends  BaseException {
    public ForbiddenException(String message)
    {
        super(403, message);
    }
}
