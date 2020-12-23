package com.jianli.business.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.business.dao.ProjectDao;
import com.jianli.business.domain.Project;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.redis.RedisUtil;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.domain.SysOrg;
import com.jianli.sys.domain.SysUser;
import com.jianli.sys.service.*;
import com.jianli.sys.workflow.domain.WorkflowActivity;
import com.jianli.sys.workflow.domain.WorkflowInstance;
import com.jianli.sys.workflow.service.IWorkflowInstanceNotice;
import com.jianli.sys.workflow.service.WorkflowEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("project")
public class ProjectService extends BaseService implements IWorkflowInstanceNotice {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    SysCodeService sysCodeService;

    @Autowired
    private SysRegionService sysRegionService;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WorkflowEngine workflowEngine;

    @Autowired
    private SysOrgService sysOrgService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public final String workflowName = "立项流程";

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);
        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());
        conditions.add(new QueryCondition("tenantId", "=", securityUtil.getTenantId(jsonRequest.getToken()).toString()));
        conditions.add(new QueryCondition("deleted", "=", "0"));
        JSONObject data = daoUtil.page(createListSQL("SELECT * FROM project"), conditions, daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));
        JSONArray entityList = data.getJSONArray("entityList");
        for (int i = 0; i < entityList.size(); i++) {
            JSONObject entity = entityList.getJSONObject(i);
            workflowEngine.getInstanceStatus(entity, entity.getLong("id"), workflowName);
        }
        data.put("projectType", sysCodeService.getCodeList("projectType"));
        data.put("budgetComposition", sysCodeService.getCodeList("budgetComposition"));
        response.setData(data);
        return response;
    }

    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        Project entity = null;
        if (Ext.isNullOrZero(id)) {
            entity = new Project();
        } else {
            entity = get(id);
        }
        response.getData().put("entity", getEntityData(entity));
        return response;
    }

    public Project get(Long id) throws Exception {
        Project entity = projectDao.selectByPrimaryKey(id);
        if (entity == null || entity.deleted) {
            throw new BadRequestException("记录不存在");
        }
        return entity;
    }

    private JSONObject getEntityData(Project entity)  throws Exception {
        JSONObject entityData = entity.toJObject();
        if(!Ext.isNullOrEmpty(entity.budgetComposition)) {
            entityData.put("budgetComposition", JSONArray.parseArray(entity.budgetComposition));
        }
        entityData.put("fileList", sysFileService.list("project", entity.id, "file" ));
        return entityData;
    }


    public String createProjectCode() throws Exception {
        long seq = redisUtil.incr("global:projectCode", 1);
        String billCode = Ext.toDateString(new Date(), "yyMMdd") + Ext.getString(String.valueOf(seq % 10000), 4);
        return billCode;
    }

    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Project entity = new Project();
        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());
        entity.projectCode = createProjectCode();
        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        projectDao.insert(entity);

        JSONArray fileList = jsonRequest.getData().getJSONArray("fileList");
        sysFileService.setEntity(fileList, "project", entity.id.toString(), "file", jsonRequest.getUserId());

        Integer saveType = jsonRequest.getData().getInteger("saveType");
        if (saveType != null && saveType.equals(2)) {
            apply(entity.id, null, jsonRequest.getUserId());
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
        Project entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        projectDao.updateByPrimaryKey(entity);

        JSONArray fileList = jsonRequest.getData().getJSONArray("fileList");
        sysFileService.setEntity(fileList, "project", entity.id.toString(), "file", jsonRequest.getUserId());

        Integer saveType = jsonRequest.getData().getInteger("saveType");
        if (saveType != null && saveType.equals(2)) {
            apply(entity.id, jsonRequest.getData().getLong("activityId"), jsonRequest.getUserId());
        }

        return response;
    }

    private void validate(JsonRequest jsonRequest) {
        jsonRequest.getData().put("budgetComposition", JSON.toJSONString(jsonRequest.getData().getJSONArray("budgetComposition")));
    }



    //workflow

    public JsonResponse getInstance(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        Long instanceId = jsonRequest.getData().getLong("instanceId");

        WorkflowInstance workflowInstance = workflowEngine.getWorkflowInstance(instanceId);

        Project entity = get(workflowInstance.entityId);

        response.getData().put("entity", getEntityData(entity));

        response.getData().put("projectType", sysCodeService.getCodeList("projectType"));
        response.getData().put("budgetComposition", sysCodeService.getCodeList("budgetComposition"));

        response.getData().put("activities", workflowEngine.listActivity(workflowInstance.entityId, workflowName));

        String activity = jsonRequest.getData().getString("activity");
        if (activity != null) {
            workflowEngine.receive(jsonRequest.getData().getLong("activityId"), null, jsonRequest.getUserId());
        }
        return response;
    }

    private void apply(Long entityId, Long activityId, Long userId) throws Exception {
        SysUser sysUser = sysUserService.get(userId);
        SysOrg sysOrg = sysOrgService.get(sysUser.orgId);
        String orgPath = sysOrg.path;
        if(orgPath.endsWith(",")) {
            orgPath = orgPath.substring(0, orgPath.length() - 1);
        }

        WorkflowInstance.Context instanceContent = new WorkflowInstance.Context();
        instanceContent.setOrgIds(orgPath);

        if (activityId != null) {
            WorkflowActivity workflowActivity = workflowEngine.getWorkflowActivity(activityId);
            workflowEngine.setWorkflowInstanceContext(workflowActivity.instanceId, instanceContent);
            workflowEngine.process(activityId, null, userId, "");
        } else if (entityId != null) {
            Project cash = get(entityId);
            workflowEngine.start(workflowName, cash.id, cash.projectCode, instanceContent, null, userId, sysUser.tenantId);
        }
    }

    public JsonResponse getApprove(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        Long activityId = jsonRequest.getData().getLong("activityId");

        workflowEngine.receive(activityId, null, jsonRequest.getUserId());

        WorkflowActivity workflowActivity = workflowEngine.getWorkflowActivity(activityId);
        WorkflowInstance workflowInstance = workflowEngine.getWorkflowInstance(workflowActivity.instanceId);

        Project entity = get(workflowInstance.entityId);

        response.getData().put("entity", getEntityData(entity));
        response.getData().put("projectType", sysCodeService.getCodeList("projectType"));
        response.getData().put("budgetComposition", sysCodeService.getCodeList("budgetComposition"));

        JSONArray activities = workflowEngine.listActivity(workflowInstance.entityId, workflowName);
        response.getData().put("activities", activities);

        JSONArray returnTos = workflowEngine.listReturnTo(activities, activityId);
        response.getData().put("returnTos", returnTos);

        response.getData().put("approveCommonWords", sysCodeService.getCodeTextList("approveCommonWords"));

        return response;
    }

    public JsonResponse saveApprove(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        JsonResponse response = JsonResponse.create(jsonRequest);

        Long activityId = jsonRequest.getData().getLong("activityId");
        Integer result = jsonRequest.getData().getInteger("result");
        String activityRemark = jsonRequest.getData().getString("activityRemark");

        if (result.equals(1)) {
            WorkflowActivity.Context activityContent = new WorkflowActivity.Context();
            JSONArray countersignUserIds = jsonRequest.getData().getJSONArray("countersignUserIds");
            if (countersignUserIds != null) {
                activityContent.setToUserIds(Ext.join(countersignUserIds.toArray(), ","));
            }

            WorkflowActivity workflowActivity = workflowEngine.getWorkflowActivity(activityId);
            WorkflowInstance workflowInstance = workflowEngine.getWorkflowInstance(workflowActivity.instanceId);
            Project entity = get(workflowInstance.entityId);

            activityContent.setCondition("金额<100000");
            if(entity.budgetAmount >= 100000)
            {
                activityContent.setCondition("金额>=100000");
            }
            workflowEngine.process(activityId, activityContent, jsonRequest.getUserId(), activityRemark);
        } else if (result.equals(0)) {
            WorkflowActivity.Context activityContent = new WorkflowActivity.Context();
            activityContent.setReturnTo(jsonRequest.getData().getString("returnTo"));
            workflowEngine.returnTo(activityId, activityContent, jsonRequest.getUserId(), activityRemark);
        }
        return response;
    }

    public void onStarted(WorkflowInstance instance, Long userId) {
        logger.info("onStarted:" + instance.instanceCode);
    }

    public void onCompleted(WorkflowInstance instance, Long userId) {
        logger.info("onCompleted:" + instance.instanceCode);
    }

    public void onStoped(WorkflowInstance instance, Long userId) {
        logger.info("onStoped:" + instance.instanceCode);
    }

}
