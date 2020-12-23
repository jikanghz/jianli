package com.jianli.common.util;

import com.jianli.common.Ext;
import com.jianli.common.exception.ForbiddenException;
import com.jianli.common.redis.RedisUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;


@Component
public class SecurityUtil {

    public  final int SESSION_TIMEOUT = 1200;

    @Resource
    private RedisUtil redisUtil;


    public Object getSessionItemValue(String sessionId, String key) {
        return redisUtil.hget("Session:" + sessionId, key);
    }

    public void setSessionItemValue(String sessionId, String key, Object value) {
        redisUtil.hset("Session:" + sessionId, key, value, SESSION_TIMEOUT);
    }

    public void setSessionItemValue(String sessionId, Map<String,Object> map) {
        redisUtil.hmset("Session:" + sessionId, map, SESSION_TIMEOUT);
    }


    public boolean hasSessionItem(String sessionId, String key)
    {

        return redisUtil.hHasKey("Session:" + sessionId, key);
    }


    public Long getUserId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        Object userId = getSessionItemValue(sessionId, "userId");
        return Ext.toLong(userId);
    }


    public Long getTenantId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        Object tenantId = getSessionItemValue(sessionId, "tenantId");
        return Ext.toLong(tenantId);
    }



    public void deleteSession(String sessionId) {
        redisUtil.del("Session:" + sessionId);
    }



    public boolean expireSession(String sessionId,long time) {
        return redisUtil.expire("Session:" + sessionId, time);
    }


    public Boolean isSupperAccount(String sessionId) {
       return  Ext.toBoolean(getSessionItemValue(sessionId, "isSupperAccount"));
    }

    public void checkDataPermission(String sessionId, Long dataTenantId)
    {
        if(!Ext.isNullOrZero(dataTenantId)) {
            if (!isSupperAccount(sessionId)) {
                Long tenantId = getTenantId(sessionId);
                if (tenantId != dataTenantId) {
                    throw new ForbiddenException("数据越界了，请谨慎操作");
                }
            }
        }
    }
}
