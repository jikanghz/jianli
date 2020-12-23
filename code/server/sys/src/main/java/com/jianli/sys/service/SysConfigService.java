package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysConfigDao;
import com.jianli.sys.dao.SysRoleDao;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.dao.SysUserRoleDao;
import com.jianli.sys.dao.lookup.OrgLookup;
import com.jianli.sys.dao.lookup.RoleLookup;
import com.jianli.sys.domain.SysConfig;
import com.jianli.sys.domain.SysRole;
import com.jianli.sys.domain.SysUser;
import com.jianli.sys.domain.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service("sysConfig")
public class SysConfigService extends BaseService {
    @Autowired
    private SysConfigDao sysConfigDao;


    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        //authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        JSONObject entity = new JSONObject();
        JSONArray banners = new JSONArray();

        SysConfig sysConfig = sysConfigDao.get("banner");
        if(sysConfig != null && sysConfig.codeValue.trim().length() > 0)
        {
            banners = JSONArray.parseArray(sysConfig.codeValue);
        }

        entity.put("banner", banners);
        response.getData().put("entity", entity);

        return response;
    }

    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        //authorization(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        String banner = jsonRequest.getData().getString("banner");

        SysConfig sysConfig = sysConfigDao.get("banner");
        if(sysConfig != null) {
            sysConfig.codeValue = banner;
        }
        else
        {
            sysConfig = new SysConfig();
            sysConfig.codeName = "banner";
            sysConfig.codeValue = banner;
        }

        sysConfigDao.updateByPrimaryKey(sysConfig);
        return response;
    }
}
