package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysOrgDao;
import com.jianli.sys.dao.lookup.*;
import com.jianli.sys.domain.SysOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;


@Service("sysOrg")
public class SysOrgService extends BaseService {


    @Autowired
    private SysOrgDao sysOrgDao;


    @Autowired
    SysCodeService sysCodeService;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    OrgLookup  orgLookup;

    @Autowired
    private SysRegionService sysRegionService;


    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        List<LinkedHashMap<String, Object>> orgs = sysOrgDao.listAll(securityUtil.getTenantId(jsonRequest.getToken()));

        JSONArray treeList = new JSONArray();
        JSONArray list = Ext.toJArray(orgs);
        toTree(treeList, list, "0", false);
        response.getData().put("entityList", treeList);

        return response;
    }


    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        SysOrg entity = null;
        if(Ext.isNullOrZero(id))
        {
            entity = new SysOrg();
            if(!Ext.isNullOrZero(jsonRequest.getData().getLong("parentId")))
            {
                entity.parentId = jsonRequest.getData().getLong("parentId");
            }
        }
        else
        {
            entity = get(id);
        }

        securityUtil.checkDataPermission(jsonRequest.getToken(), entity.tenantId);

        JSONObject entityData = entity.toJObject();
        entityData.put("regionCode", sysRegionService.getParentRegionCodes(entity.regionCode));

        response.getData().put("entity", entityData);

        List<LinkedHashMap<String, Object>> orgs = sysOrgDao.listAll(securityUtil.getTenantId(jsonRequest.getToken()));
        JSONArray treeList = new JSONArray();
        JSONArray orgList = Ext.toJArray(orgs);
        toTree(treeList, orgList, "0", false);
        response.getData().put("parentId",treeList);

        //orgLookup.fillCodeTable(response.getData(), entityData, "parentId", securityUtil.getTenantId(jsonRequest.getToken()));

        return response;
    }


    public SysOrg get(Long id) throws Exception
    {
        SysOrg entity = sysOrgDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("机构不存在");
        }
        return entity;
    }


    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);
        SysOrg entity = new SysOrg();


        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());
        entity.orgType = 10;

        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        sysOrgDao.insert(entity);

        entity.SetPath(sysOrgDao.getPath(entity.parentId));
        sysOrgDao.updateByPrimaryKey(entity);

        return response;
    }

    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysOrg entity = get(id);

        securityUtil.checkDataPermission(jsonRequest.getToken(), entity.tenantId);

        if(entity.parentId.equals(0))
        {
            if(!Ext.isNullOrZero(jsonRequest.getData().getLong("parentId")))
            {
                throw  new BadRequestException("总部不能设置上级机构");
            }
        }

        entity.set(jsonRequest.getData());

        if(entity.parentId.equals(entity.id))
        {
            throw  new BadRequestException("上级机构不能和本机构相同");
        }

        entity.setDefault(jsonRequest.getUserId());

        if(entity.parentId.equals(0))
        {
            entity.SetPath("");
        }
        else {
            entity.SetPath(sysOrgDao.getPath(entity.parentId));
        }

        sysOrgDao.updateByPrimaryKey(entity);

        return response;
    }

    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysOrg entity = get(id);

        entity.deleted = true;
        entity.setDefault(jsonRequest.getUserId());

        sysOrgDao.updateByPrimaryKey(entity);

        return response;
    }



    public void toTree(JSONArray treeList, JSONArray list, String parentId, boolean allowEmpty) {
        for (int i = 0; i < list.size(); ++i) {
            JSONObject module = list.getJSONObject(i);
            if (module.getString("parentId").equals(parentId)) {
                JSONArray childModules = new JSONArray();
                toTree(childModules, list, module.getString("id"), allowEmpty);
                if (allowEmpty || childModules.size() > 0) {
                    module.put("children", childModules);
                }
                treeList.add(module);
            }
        }
    }


    private void validate(JsonRequest jsonRequest) {
        JSONArray regionCodes = jsonRequest.getData().getJSONArray("regionCode");
        if (regionCodes == null || regionCodes.size() < 1) {
            jsonRequest.getData().put("regionCode", "");
        } else {
            jsonRequest.getData().put("regionCode", regionCodes.getString(regionCodes.size() - 1));
        }
    }

}
