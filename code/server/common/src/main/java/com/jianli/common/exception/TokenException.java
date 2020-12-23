package com.jianli.common.exception;

/**
 *   Token验证异常类
 */
public class TokenException extends  BaseException {
    public TokenException(String message)
    {
        super(302, message);
    }
}
