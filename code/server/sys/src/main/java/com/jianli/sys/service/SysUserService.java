package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.*;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysOrgDao;
import com.jianli.sys.dao.SysRoleDao;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.dao.SysUserRoleDao;
import com.jianli.sys.dao.lookup.*;
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


@Service("sysUser")
public class SysUserService extends BaseService {

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysCodeService sysCodeService;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private SysRegionService sysRegionService;

    @Autowired
    private SysRoleDao sysRoleDao;

    @Autowired
    private RoleLookup roleLookup;

    @Autowired
    OrgLookup orgLookup;

    @Autowired
    SysUserRoleDao sysUserRoleDao;

    @Autowired
    private SysOrgDao sysOrgDao;

    @Autowired
    private SysOrgService sysOrgService;

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("deleted", "=", "0"));

        conditions.add(new QueryCondition("tenantId", "=", securityUtil.getTenantId(jsonRequest.getToken()).toString(), true));

        JSONObject data = daoUtil.page(createListSQL("SELECT sys_user.id,sys_user.userName,sys_user.orgId,sys_user.loginName,sys_user.gender,sys_user.mobile,sys_user.email,sys_user.qq,sys_user.imageUrl,sys_user.tenantId,sys_user.status,sys_user.createBy,sys_user.createTime,sys_user.updateBy,sys_user.updateTime,sys_user.remark,sys_user.deleted,group_concat(sys_role.roleName) as roleName FROM sys_user LEFT JOIN sys_user_role ON sys_user_role.userId = sys_user.id LEFT JOIN sys_role ON sys_role.id = sys_user_role.roleId WHERE sys_user.deleted=0 AND sys_user_role.deleted = 0 AND sys_role.deleted = 0 group by sys_user.id"),
                conditions, daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        data.put("status", sysCodeService.getCodeList("status"));
        data.put("gender", sysCodeService.getCodeList("gender"));
        List<LinkedHashMap<String, Object>> roles = roleLookup.search(jsonRequest);
        data.put("roleIds", Ext.toJArray(roles));
        List<LinkedHashMap<String, Object>> orgs = orgLookup.search(jsonRequest);
        data.put("parentId", Ext.toJArray(orgs));


        response.setData(data);


        return response;
    }

    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        SysUser entity = null;
        if (Ext.isNullOrZero(id)) {
            entity = new SysUser();
        } else {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();
        entityData.remove("password");
        entityData.put("regionCode", sysRegionService.getParentRegionCodes(entity.regionCode));

        List<SysRole> userRoles = sysRoleDao.listUserRole(id);
        JSONArray jsonUserRoles = new JSONArray();
        for (SysRole userRole : userRoles) {
            jsonUserRoles.add(Ext.toInteger(userRole.id));
        }
        entityData.put("roleIds", jsonUserRoles);
        List<LinkedHashMap<String, Object>> orgs = sysOrgDao.listAll(securityUtil.getTenantId(jsonRequest.getToken()));
        JSONArray treeList = new JSONArray();
        JSONArray list = Ext.toJArray(orgs);
        sysOrgService.toTree(treeList, list, "0", false);
        response.getData().put("orgId", treeList);
        response.getData().put("entity", entityData);
        return response;
    }

    public SysUser get(Long id) throws Exception {
        SysUser entity = sysUserDao.selectByPrimaryKey(id);
        if (entity == null || entity.deleted) {
            throw new BadRequestException("用户不存在");
        }
        return entity;
    }


    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        SysUser entity = new SysUser();

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        if (!Ext.isNullOrEmpty(jsonRequest.getData().getString("password"))) {
            entity.password = Ext.md5(jsonRequest.getData().getString("password"));
        } else {
            throw new BadRequestException("新建用户时，密码不能为空");
        }

        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        sysUserDao.insert(entity);

        List<SysUserRole> userRoles = CreateUserRoleList(jsonRequest.getData().getJSONArray("roleIds"), entity.id, jsonRequest.getUserId());

        if (userRoles.size() > 0) {
            sysUserRoleDao.insertList(userRoles);
        }

        return response;
    }

    @Transactional
    public JsonResponse update(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysUser entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysUserDao.updateByPrimaryKey(entity);

        List<SysUserRole> userRoles = CreateUserRoleList(jsonRequest.getData().getJSONArray("roleIds"), entity.id, jsonRequest.getUserId());
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        List<SysUserRole> dbUserRoles = sysUserRoleDao.listUserRole(id);
        List<Long> dbRoleIds = dbUserRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        List<SysUserRole> insertItems = userRoles.stream().filter(userRole -> !dbRoleIds.contains(userRole.roleId)).collect(Collectors.toList());

        List<SysUserRole> updateItems = dbUserRoles.stream().filter(dbUserRole -> roleIds.contains(dbUserRole.roleId)).collect(Collectors.toList());
        List<SysUserRole> deleteItems = dbUserRoles.stream().filter(dbUserRole -> !roleIds.contains(dbUserRole.roleId)).collect(Collectors.toList());


        if (insertItems.size() > 0) {
            sysUserRoleDao.insertList(insertItems);
        }

        for (SysUserRole item : updateItems) {
            item.setDefault(jsonRequest.getUserId());
            sysUserRoleDao.updateByPrimaryKey(item);
        }

        for (SysUserRole item : deleteItems) {
            item.setDefault(jsonRequest.getUserId());
            item.deleted = true;
            sysUserRoleDao.updateByPrimaryKey(item);
        }

        return response;
    }

    public JsonResponse delete(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysUser entity = get(id);

        entity.setDefault(jsonRequest.getUserId());
        entity.deleted = true;
        sysUserDao.updateByPrimaryKey(entity);

        return response;
    }

    private void validate(JsonRequest jsonRequest) {
        JSONArray regionCodes = jsonRequest.getData().getJSONArray("regionCode");
        if (regionCodes == null || regionCodes.size() < 1) {
            jsonRequest.getData().put("regionCode", "");
        } else {
            jsonRequest.getData().put("regionCode", regionCodes.getString(regionCodes.size() - 1));
        }

        daoUtil.checkExists("sys_user", "loginName", jsonRequest.getData().getString("loginName"), "登录名", "id", jsonRequest.getData().get("id"), null);
        daoUtil.checkExists("sys_user", "mobile", jsonRequest.getData().getString("mobile"), "手机号", "id", jsonRequest.getData().get("id"), null);
    }

    private List<SysUserRole> CreateUserRoleList(JSONArray roleIdList, Long ownerUserId, Long userId) throws Exception {

        Object[] roleIds = roleIdList.toArray();
        List<SysUserRole> userRoles = new LinkedList<SysUserRole>();
        for (Object roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setDefault(userId);
            userRole.roleId = Ext.toLong(roleId);
            userRole.userId = ownerUserId;
            userRoles.add(userRole);
        }
        return userRoles;
    }

}
