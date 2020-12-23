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
import com.jianli.sys.dao.*;
import com.jianli.sys.dao.lookup.OrgLookup;
import com.jianli.sys.dao.lookup.RoleLookup;
import com.jianli.sys.domain.SysLog;
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


@Service("sysLog")
public class SysLogService extends BaseService {

    @Autowired
    private SysLogDao sysLogDao;

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);
        return response;
    }

    public void insert(SysLog syslog){
        sysLogDao.insert(syslog);
    }

}
