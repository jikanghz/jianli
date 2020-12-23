package com.jianli.common.exception;

/**
 *   自定义异常基类
 */
public class BaseException extends RuntimeException {
    private int code;

    public  int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public BaseException(int code, String message)  {
        super(message);
        setCode(code);
    }
}
