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
import com.jianli.sys.domain.Notice;
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

@Service("notice")
public class NoticeService extends BaseService {

    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    private SecurityUtil securityUtil;


    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("deleted","=","0"));
        conditions.add(new QueryCondition("tenantId", "=", securityUtil.getTenantId(jsonRequest.getToken()).toString(), true));

        JSONObject data = daoUtil.page("SELECT id,title,status,createBy,createTime,updateBy,updateTime,remark FROM notice",
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        response.setData(data);
        return response;
    }

    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        Notice entity = null;
        if(Ext.isNullOrZero(id))
        {
            entity = new Notice();
        }
        else
        {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();

        response.getData().put("entity", entityData);
        return response;
    }

    public Notice get(Long id) throws Exception
    {
        Notice entity = noticeDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("数据不存在");
        }
        return entity;
    }

    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Notice entity = new Notice();

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        noticeDao.insert(entity);
        return response;
    }


    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Notice entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        noticeDao.updateByPrimaryKey(entity);

        return response;
    }


    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Notice entity = get(id);

        entity.setDefault(jsonRequest.getUserId());
        entity.deleted = true;
        noticeDao.updateByPrimaryKey(entity);

        return response;
    }

    public JsonResponse frontList(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("deleted","=","0"));
        conditions.add(new QueryCondition("tenantId", "=", securityUtil.getTenantId(jsonRequest.getToken()).toString(), true));

        JSONObject data = daoUtil.page("SELECT id,title,status,createBy,createTime,updateBy,updateTime,remark FROM notice",
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        response.setData(data);
        return response;
    }

    public JsonResponse detail(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Notice entity = null;
        if(Ext.isNullOrZero(id))
        {
            entity = new Notice();
        }
        else
        {
            entity = get(id);
        }
        JSONObject entityData = entity.toJObject();
        response.getData().put("entity", entityData);
        return response;
    }
}
