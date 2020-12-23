package com.jianli.common.service;

import com.jianli.common.Ext;
import com.jianli.common.exception.ForbiddenException;
import com.jianli.common.exception.UnauthorizedException;
import com.jianli.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public abstract class BaseService {
    @Autowired
    private SecurityUtil securityUtil;

    protected static HashMap<String, String> serviceMap = new HashMap<String, String>();
    protected static HashMap<String, String> methodMap = new HashMap<String, String>();

    public void authentication(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest.getToken());
    }

    public void authentication(String token) {
        Long userId = securityUtil.getUserId(token);
        if (Ext.isNullOrZero(userId)) {
            throw new UnauthorizedException("请重新登录后再试");
        }

        Object client = securityUtil.getSessionItemValue(token, "client");
        if (!Ext.isNullOrEmpty(client)) {
            securityUtil.expireSession(token, securityUtil.SESSION_TIMEOUT);
        }
    }


    public void authorization(JsonRequest jsonRequest) throws Exception {
        authorization(jsonRequest.getToken(), jsonRequest.getService(), jsonRequest.getMethod());
    }

    public void authorization(String token, String service, String method) throws Exception {
        String permission = "permission_" + service + "_" + method;
        if (!securityUtil.hasSessionItem(token, permission)) {
            throw new ForbiddenException("您对" + getServiceDisplayName(service) + "没有" + getMethodDisplayName(method) + "的权限");
        }
    }

    protected void addService(String service, String displayName) {
        serviceMap.put(service, displayName);
    }

    public static String getServiceDisplayName(String service) {
        if (serviceMap.containsKey(service)) {
            return serviceMap.get(service);
        }
        return service;
    }

    protected void addMethod(String method, String displayName) {
        methodMap.put(method, displayName);
    }

    public static String getMethodDisplayName(String method) {
        if (methodMap.containsKey(method)) {
            return methodMap.get(method);
        }
        return method;
    }

    protected String createListSQL(String sql) {
        return "SELECT * FROM (" + sql + ") T";
    }
}
