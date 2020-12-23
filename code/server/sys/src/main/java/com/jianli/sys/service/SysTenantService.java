package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.excel.Excel;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.sys.dao.SysTenantDao;
import com.jianli.sys.dao.SysOrgDao;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.dao.SysUserRoleDao;
import com.jianli.sys.domain.SysTenant;
import com.jianli.sys.domain.SysOrg;
import com.jianli.sys.domain.SysUser;
import com.jianli.sys.domain.SysUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service("sysTenant")
public class SysTenantService extends BaseService {

    @Autowired
    SysTenantDao sysTenantDao;

    @Autowired
    SysCodeService sysCodeService;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    private SysRegionService sysRegionService;

    @Autowired
    private SysOrgDao sysOrgDao;

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("deleted","=","0"));

        JSONObject data = daoUtil.page("SELECT id,tenantName,contactName,contactMobile,contactEmail,address,status,remark,createBy,createTime FROM sys_tenant",
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        data.put("status", sysCodeService.getCodeList("status") );


        /*
        JSONArray regionList = Ext.toJArray(sysRegionService.listAll(3));
        JSONArray regionTree = new JSONArray();
        sysRegionService.toTree(regionTree, regionList, "1", false);
        data.put("regionData", regionTree);*/

        response.setData(data);

        return response;
    }

    public JsonResponse export(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JSONObject page = new JSONObject();
        page.put("pageNumber", 1);
        page.put("pageSize", 50000);
        jsonRequest.getData().put("page", page);

        JsonResponse response = list(jsonRequest);

        JSONArray cols = new JSONArray();
        response.getData().put("cols", cols);

        cols.add(Excel.createExcelColumn("tenantName", "单位名称"));
        cols.add(Excel.createExcelColumn("contactName", "联系人姓名"));
        cols.add(Excel.createExcelColumn("contactMobile", "联系人手机"));
        cols.add(Excel.createExcelColumn("contactEmail", "联系人邮箱"));
        cols.add(Excel.createExcelColumn("address", "地址"));
        cols.add(Excel.createExcelColumn("status", "状态"));
        cols.add(Excel.createExcelColumn("remark", "备注"));
        cols.add(Excel.createExcelColumn("createTime", "新建时间", 6000));

        response.getData().put("FileName", "单位-" + Ext.toDateString(new Date(), "yyyyMMdd") + ".xls");
        JSONArray entityList = response.getData().getJSONArray("entityList");
        JSONArray statusCodeList = response.getData().getJSONArray("status");

        for (int i=0; i<entityList.size(); ++ i)
        {
            JSONObject entity = entityList.getJSONObject(i);
            entity.put("createTime", Ext.toDateString(entity.getString("createTime"),"yyyy-MM-dd HH:mm:ss"));
            entity.put("status", sysCodeService.getCodeName(statusCodeList, entity.getString("status")));
        }

        return response;
    }


    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        SysTenant entity = null;
        if (Ext.isNullOrZero(id)) {
            entity = new SysTenant();
        } else {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();

        entityData.put("regionCode", sysRegionService.getParentRegionCodes(entity.regionCode));
        response.getData().put("entity", entityData);

        return response;
    }

    private SysTenant get(Long id) throws Exception
    {
        SysTenant entity = sysTenantDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("单位不存在");
        }
        return entity;
    }


    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        SysTenant entity = new SysTenant();

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysTenantDao.insert(entity);

        SysOrg sysOrg = new SysOrg();
        sysOrg.setDefault(jsonRequest.getUserId());
        sysOrg.tenantId = entity.id;
        sysOrg.orgType = 1;
        sysOrg.orgName = "总部";
        sysOrg.parentId = Long.valueOf(0);
        sysOrgDao.insert(sysOrg);



        SysUser sysUser = new SysUser();
        sysUser.setDefault(jsonRequest.getUserId());
        sysUser.orgId = sysOrg.id;
        sysUser.tenantId = entity.id;
        sysUser.userName = "单位管理员";
        sysUser.loginName = "admin@" + sysUser.tenantId;
        sysUser.password = Ext.md5(Ext.md5(sysUser.loginName).substring(0, 6));

        sysUserDao.insert(sysUser);

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setDefault(jsonRequest.getUserId());
        sysUserRole.userId = sysUser.id;
        sysUserRole.roleId = Long.valueOf(2);
        sysUserRoleDao.insert(sysUserRole);

        return response;
    }


    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysTenant entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysTenantDao.updateByPrimaryKey(entity);

        return response;
    }


    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysTenant entity = get(id);

        entity.setDefault(jsonRequest.getUserId());
        entity.deleted = true;

        sysTenantDao.updateByPrimaryKey(entity);

        return response;
    }

    private void validate(JsonRequest jsonRequest)
    {
        JSONArray regionCodes = jsonRequest.getData().getJSONArray("regionCode");
        if(regionCodes == null || regionCodes.size() < 1)
        {
            jsonRequest.getData().put("regionCode", "");
        }
        else
        {
            jsonRequest.getData().put("regionCode", regionCodes.getString(regionCodes.size()-1));
        }
    }
}
