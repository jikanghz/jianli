package com.jianli.common.controller;


import com.jianli.common.exception.InternalKnownException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

@Component
public class ControllerUtil implements InitializingBean {

    @Value("${web.logApis:}")
    private String logApis;
    private Set logSet = new HashSet<>();

    @Value("${web.protectedDemoApis:}")
    private String protectedDemoApis;
    private Set protectedDemoSet = new HashSet<>();

    public void afterPropertiesSet() throws Exception {
        String[] logList = logApis.split(",");
        for(String log : logList)
        {
            if(!logSet.contains(log.trim()))
            {
                logSet.add(log.trim());
            }
        }

        String[] protectedDemoApiList = protectedDemoApis.split(",");
        for(String protectedDemoApi : protectedDemoApiList)
        {
            if(!protectedDemoSet.contains(protectedDemoApi.trim()))
            {
                protectedDemoSet.add(protectedDemoApi.trim());
            }
        }
    }

    public boolean needLog(String api)
    {
        if(logSet.contains(api))
        {
            return true;
        }
        return false;
    }

    public void protectedDemo(String api)
    {
        if(protectedDemoSet.contains(api))
        {
            throw new InternalKnownException("演示环境暂不支持本操作");
        }
    }


    public String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getToken(HttpServletRequest request) {
        String token = getCookieValue(request, "apiToken");
        if (token == null) {
            token = "";
        }
        return token;
    }

    public String getIpAdrress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
