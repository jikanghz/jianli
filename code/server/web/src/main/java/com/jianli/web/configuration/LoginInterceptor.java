package com.jianli.web.configuration;

import com.jianli.common.Ext;
import com.jianli.common.exception.UnauthorizedException;
import com.jianli.common.util.SecurityUtil;
import com.jianli.common.controller.ControllerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ControllerUtil controllerUtil;

    public LoginInterceptor()
    {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException, ServletException {
        try {
            String token = controllerUtil.getToken(request);
            Long userId = securityUtil.getUserId(token);
            if (Ext.isNullOrZero(userId)) {
                throw new UnauthorizedException("请重新登录后再试");
            }
        }
        catch (Exception ex) {
            request.getRequestDispatcher("/login").forward(request,response);
            return false;
        }
        return true;
    }
}
