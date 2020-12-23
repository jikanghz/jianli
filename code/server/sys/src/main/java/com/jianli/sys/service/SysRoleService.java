package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.exception.ForbiddenException;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysRoleDao;
import com.jianli.sys.dao.SysRoleModuleDao;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.dao.lookup.*;
import com.jianli.sys.domain.SysRole;
import com.jianli.sys.domain.SysRoleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service("sysRole")
public class SysRoleService extends BaseService {

    @Autowired
    private SysRoleDao sysRoleDao;

    @Autowired
    private SysRoleModuleDao sysRoleModuleDao;


    @Autowired
    private SysModuleService sysModuleService;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    SysCodeService sysCodeService;

    @Autowired
    SecurityService securityService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TenantLookup tenantLookup;

    @Autowired
    private SysUserDao sysUserDao;


    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("tenantId", "IN", "(0," + securityUtil.getTenantId(jsonRequest.getToken()) + ")", false));

        conditions.add(new QueryCondition("deleted", "=", "0"));

        JSONObject data = daoUtil.page("SELECT * FROM sys_role",
                conditions, daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        data.put("status", sysCodeService.getCodeList("status"));

        JSONArray entityList = data.getJSONArray("entityList");
        tenantLookup.fillCodeTables(data, entityList, "tenantId");


        response.setData(data);

        return response;
    }


    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysRole entity = null;
        if (Ext.isNullOrZero(id)) {
            entity = new SysRole();
        } else {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();
        response.getData().put("entity", entityData);

        List<LinkedHashMap<String, Object>> modules = securityService.getUserModules(jsonRequest.getUserId(), 4);
        securityService.getParentModules(modules);

        JSONArray moduleTreeList = new JSONArray();
        JSONArray moduleList = Ext.toJArray(modules);
        sysModuleService.toTree(moduleTreeList, moduleList, "0", false);
        response.getData().put("moduleList", moduleTreeList);

        JSONArray moduleIds = new JSONArray();
        List<SysRoleModule> sysRoleModuleList = sysRoleModuleDao.listRoleModule(id);
        for (int i = 0; i < sysRoleModuleList.size(); ++i) {
            moduleIds.add(sysRoleModuleList.get(i).moduleId);
        }
        response.getData().put("moduleIds", moduleIds);
        return response;
    }

    private SysRole get(Long id) throws Exception {
        SysRole entity = sysRoleDao.selectByPrimaryKey(id);
        if (entity == null || entity.deleted) {
            throw new BadRequestException("角色不存在");
        }
        return entity;
    }


    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        SysRole entity = new SysRole();
        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());
        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());
        sysRoleDao.insert(entity);

        List<SysRoleModule> roleModules = CreateRoleModuleList(jsonRequest.getData().getJSONArray("moduleIds"), entity.id, jsonRequest.getUserId());

        if (roleModules.size() > 0) {
            sysRoleModuleDao.insertList(roleModules);

        }
        return response;
    }


    @Transactional
    public JsonResponse update(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysRole entity = get(id);

        if (!securityUtil.isSupperAccount(jsonRequest.getToken())) {
            if(!securityUtil.getTenantId(jsonRequest.getToken()).equals(entity.tenantId))
            {
                throw new BadRequestException("公共角色不能修改");
            }
        }

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysRoleDao.updateByPrimaryKey(entity);

        List<SysRoleModule> roleModules = CreateRoleModuleList(jsonRequest.getData().getJSONArray("moduleIds"), entity.id, jsonRequest.getUserId());
        List<Long> moduleIds = roleModules.stream().map(SysRoleModule::getModuleId).collect(Collectors.toList());

        List<SysRoleModule> dbRoleModules = sysRoleModuleDao.listRoleModule(id);
        List<Long> dbModuleIds = dbRoleModules.stream().map(SysRoleModule::getModuleId).collect(Collectors.toList());

        List<SysRoleModule> insertItems = roleModules.stream().filter(roleModule ->!dbModuleIds.contains(roleModule.moduleId)).collect(Collectors.toList());
        List<SysRoleModule> updateItems = dbRoleModules.stream().filter(dbRoleModule ->moduleIds.contains(dbRoleModule.moduleId)).collect(Collectors.toList());
        List<SysRoleModule> deleteItems = dbRoleModules.stream().filter(dbRoleModule ->!moduleIds.contains(dbRoleModule.moduleId)).collect(Collectors.toList());

        if (insertItems.size() > 0) {
            sysRoleModuleDao.insertList(insertItems);
        }

        for (SysRoleModule item : updateItems)
        {
            item.setDefault(jsonRequest.getUserId());
            sysRoleModuleDao.updateByPrimaryKey(item);
        }

        for (SysRoleModule item : deleteItems)
        {
            item.deleted = true;
            item.setDefault(jsonRequest.getUserId());
            sysRoleModuleDao.updateByPrimaryKey(item);
        }

        return response;
    }


    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysRole entity = get(id);

        if (!sysUserDao.isSupperAccount((jsonRequest.getUserId()))) {
            if(!securityUtil.getTenantId(jsonRequest.getToken()).equals(entity.tenantId))
            {
                throw new BadRequestException("公共角色不能删除");
            }
        }

        entity.deleted = true;
        entity.setDefault(jsonRequest.getUserId());

        sysRoleDao.updateByPrimaryKey(entity);

        return response;
    }


    private List<SysRoleModule> CreateRoleModuleList(JSONArray moduleIdList, Long roleId, Long userId) throws Exception {

        Object[] moduleIds = moduleIdList.toArray();
        List<SysRoleModule> roleModules = new LinkedList<SysRoleModule>();
        for (Object moduleId : moduleIds) {
            SysRoleModule roleModule = new SysRoleModule();
            roleModule.setDefault(userId);
            roleModule.moduleId = Ext.toLong(moduleId);
            roleModule.roleId = roleId;
            roleModules.add(roleModule);
        }
        return roleModules;
    }

}
