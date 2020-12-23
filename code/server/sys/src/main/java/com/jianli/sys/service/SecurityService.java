package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.jianli.common.Ext;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.exception.UnauthorizedException;
import com.jianli.common.service.*;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysConfigDao;
import com.jianli.sys.dao.SysModuleDao;
import com.jianli.sys.dao.SysOrgDao;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.domain.SysConfig;
import com.jianli.sys.domain.SysOrg;
import com.jianli.sys.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 安全服务
 */
@Service("security")
public class SecurityService extends BaseService {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysModuleDao sysModuleDao;

    @Autowired
    private SysModuleService sysModuleService;

    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SysOrgDao sysOrgDao;


    /**
     * 登录
     *
     * @param jsonRequest， json请求
     * @return JsonResponse, json响应
     */
    public JsonResponse login(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);

        String loginName = jsonRequest.getData().getString("loginName");
        String password = jsonRequest.getData().getString("password");

        SysUser sysUser = sysUserDao.getByLoginName(loginName);
        if (sysUser == null) {
            throw new UnauthorizedException("帐号或密码错误");
        }

        if (!Ext.md5(password).equals(sysUser.password)) {
            throw new UnauthorizedException("帐号或密码错误");
        }

        String token = Ext.getUUID();
        response.getData().put("token", token);


        Map items = new HashMap();
        items.put("userId", sysUser.id);
        items.put("client", jsonRequest.getClient());
        items.put("tenantId", sysUser.tenantId);
        items.put("userId", sysUser.id);

        SysOrg org = sysOrgDao.selectByPrimaryKey(sysUser.orgId);
        items.put("orgId", org.id);
        items.put("orgPath", org.path);
        items.put("orgType", org.orgType);

        if (sysUserDao.isSupperAccount(sysUser.id)) {
            items.put("isSupperAccount", true);
        } else {
            items.put("isSupperAccount", false);
        }

        List<LinkedHashMap<String, Object>> modules = getUserModules(sysUser.id, 4);
        for (Map<String, Object> module : modules) {
            if (!Ext.isNullOrEmpty(module.get("service")) && !Ext.isNullOrEmpty(module.get("method"))) {
                String permission = "permission_" + module.get("service") + "_" + module.get("method");
                if (!items.containsKey(permission)) {
                    items.put(permission, 1);
                }
            }
        }
        securityUtil.setSessionItemValue(token, items);

        return response;
    }

    /**
     * 获取用户主页面数据，包括用户菜单、用户姓名...
     *
     * @param jsonRequest， json请求
     * @return JsonResponse, json响应
     */
    public JsonResponse getMainData(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        long userId = Ext.toLong(jsonRequest.getUserId());

        SysUser user = sysUserService.get(userId);

        List<LinkedHashMap<String, Object>> modules = getUserModules(userId, 4);

        //上级节点如果未包含在用户权限中，则也要包含进来
        getParentModules(modules);

        for(int i=modules.size()-1; i>=0; --i)
        {
            if(modules.get(i) != null) {
                if (modules.get(i).get("moduleType").equals(4)) {
                    modules.remove(i);
                }
            }
        }

        for (LinkedHashMap<String, Object> module : modules) {
            if (module != null) {
                module.put("id", module.get("id").toString());
            }
        }


        JSONArray moduleTreeList = new JSONArray();

        JSONArray moduleList = Ext.toJArray(modules);

        sysModuleService.toTree(moduleTreeList, moduleList, "0", true);

        response.getData().put("moduleList", moduleTreeList);
        response.getData().put("userName", user.userName);
        return response;
    }

    /**
     * 身份验证和授权验证
     *
     * @param jsonRequest， json请求
     */
    public JsonResponse auth(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);
        authentication(jsonRequest);

        String token = jsonRequest.getToken();
        String service = Ext.getString(jsonRequest.getData(), "service");
        String method = Ext.getString(jsonRequest.getData(), "method");

        if (!Ext.isNullOrEmpty(token) && !Ext.isNullOrEmpty(service) && !Ext.isNullOrEmpty(method)) {
            authorization(token, service, method);
        }
        return response;
    }


    /**
     * 安全退出
     *
     * @param jsonRequest， json请求
     */
    public JsonResponse logout(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);
        logout(jsonRequest.getToken());
        return response;
    }

    /**
     * 安全退出
     *
     * @param token， token
     */
    public void logout(String token) {
        securityUtil.deleteSession(token);
    }

    /**
     * 取得默认Token，主要是为了小程序免登录，通过审核
     */
    public JsonResponse getDefaultToken(JsonRequest jsonRequest) {
        JsonResponse response = JsonResponse.create(jsonRequest);
        SysConfig sysConfig = sysConfigDao.get("defaultToken");
        String token = "";
        if (sysConfig != null) {
            token = sysConfig.codeValue;
        }
        response.getData().put("token", token);
        return response;
    }


    /**
     * 修改密码
     *
     * @param jsonRequest， json请求
     */
    public JsonResponse updatePassword(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);

        String oldPassword = jsonRequest.getData().getString("oldPassword");
        String newPassword = jsonRequest.getData().getString("newPassword");
        String confirmPassword = jsonRequest.getData().getString("confirmPassword");

        Ext.checkRequired(oldPassword, "旧密码");
        Ext.checkRequired(newPassword, "新密码");
        Ext.checkRequired(confirmPassword, "再次输入密码");

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("新密码两次输入不一致");
        }

        SysUser sysUser = sysUserDao.selectByPrimaryKey(jsonRequest.getUserId());
        if (sysUser != null) {
            if (!Ext.md5(oldPassword).equals(sysUser.password)) {
                throw new BadRequestException("旧密码不正确");
            }
            sysUser.password = Ext.md5(newPassword);
            sysUser.setDefault(jsonRequest.getUserId());
            sysUserDao.updateByPrimaryKey(sysUser);
        }
        return response;
    }

    /**
     * 获取用户相关的功能模块
     *
     * @param userId， 系统用户id
     * @return 功能模块列表
     */
    public List<LinkedHashMap<String, Object>> getUserModules(long userId, int maxModuleType) {
        List<LinkedHashMap<String, Object>> modules = null;
        if (sysUserDao.isSupperAccount((userId))) {
            modules = sysModuleDao.listAllSimple(maxModuleType);
        } else {
            modules = sysModuleDao.listUserModule(userId, maxModuleType);
        }
        if (modules == null) {
            modules = new ArrayList<LinkedHashMap<String, Object>>();
        }
        return modules;
    }

    /**
     * 获取现有模块未包含的上级模块
     *
     * @param modules， 现有模块列表
     */
    public void getParentModules(List<LinkedHashMap<String, Object>> modules) {
        for (int i = 0; i < modules.size() && modules.get(i) != null; ++i) {
            Long parentId = Ext.toLong(modules.get(i).get("parentId"));
            if (!Ext.isNullOrZero(parentId)) {
                if (getModule(modules, parentId) == null) {
                    LinkedHashMap<String, Object> module = sysModuleDao.get(parentId);
                    if (module != null) {
                        modules.add(module);
                    }
                }
            }
        }
    }

    /**
     * 查找模块
     *
     * @param modules， 模块
     * @param modules， 现有模块列表
     * @param id，      模块id
     * @return 模块对象
     */
    private LinkedHashMap<String, Object> getModule(List<LinkedHashMap<String, Object>> modules, Long id) {
        for (LinkedHashMap<String, Object> module : modules) {
            if (module == null) {
                continue;
            }
            if (!Ext.isNullOrEmpty(module.get("id"))) {
                if (Ext.toLong(module.get("id")).equals(id)) {
                    return module;
                }
            }
        }
        return null;
    }

}
