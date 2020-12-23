package com.jianli.sys.workflow.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.dao.DaoUtil;
import com.jianli.common.dao.QueryCondition;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.redis.RedisUtil;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.lookup.UserLookup;
import com.jianli.sys.domain.SysUser;
import com.jianli.sys.service.SysCodeService;
import com.jianli.sys.service.SysUserService;
import com.jianli.sys.workflow.dao.WorkflowActivityDao;
import com.jianli.sys.workflow.domain.WorkflowActivity;
import com.jianli.sys.workflow.domain.WorkflowInstance;
import com.jianli.sys.workflow.domain.WorkflowStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service("workflowActivity")
public class WorkflowActivityService extends BaseService {
    @Autowired
    private WorkflowActivityDao workflowActivityDao;

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserLookup userLookup;

    @Autowired
    private SysUserService sysUserService;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SysCodeService sysCodeService;

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());

        JsonResponse response = JsonResponse.create(jsonRequest);
        conditions.add(new QueryCondition("userId", "=", jsonRequest.getUserId().toString()));
        conditions.add(new QueryCondition("status", "IN", "(1,2)", false));

        JSONObject data = daoUtil.page(createListSQL("SELECT workflow_activity.id, workflow_activity.status, workflow.workflowName, workflow_activity.workflowId, workflow_activity.instanceId, workflow_instance.instanceCode, workflow_instance.createBy, workflow_activity.fromStepId, workflow_activity.stepId, workflow_step.stepName, workflow_step.activityUrl, workflow_instance.createTime,workflow_activity.userId FROM workflow_instance LEFT JOIN workflow_activity ON workflow_instance.id = workflow_activity.instanceId AND workflow_instance.flowId = workflow_activity.flowId LEFT JOIN workflow ON workflow.id = workflow_activity.workflowId LEFT JOIN workflow_step ON workflow_step.id = workflow_activity.stepId WHERE workflow_step.stepType != 3"),
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        JSONArray entityList = data.getJSONArray("entityList");

        data.put("status", sysCodeService.getCodeList("workflowActivityStatus") );
        userLookup.fillCodeTables(data, entityList, "createBy");

        response.setData(data);

        return response;
    }

    public WorkflowActivity get(Long id) throws Exception
    {
        WorkflowActivity entity = workflowActivityDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("流程活动不存在");
        }

        return entity;
    }

    WorkflowActivity newActivity(Long workflowId, Long instanceId, String instanceCode, Long fromStepId, Long stepId, Long flowId, Long userId, Long tenantId, WorkflowActivity.Context activityContext)
    {
        WorkflowActivity newActivity = new WorkflowActivity();
        newActivity.workflowId = workflowId;
        newActivity.instanceId = instanceId;
        newActivity.instanceCode = instanceCode;
        newActivity.fromStepId = fromStepId;
        newActivity.stepId = stepId;
        newActivity.flowId = flowId;
        newActivity.userId = Ext.toLong(userId);
        newActivity.tenantId = tenantId;
        newActivity.setContext(activityContext);
        newActivity.setDefault(userId);
        return newActivity;
    }

    List<WorkflowActivity> listActivityByStepId(Long instanceId, Long stepId)
    {
        List<WorkflowActivity> items = workflowActivityDao.list(instanceId);
        List<WorkflowActivity> activities = new ArrayList<>();
        Long flowId = null;
        for (WorkflowActivity item : items) {
            if(item.stepId.equals(stepId)) {
                if(flowId == null) {
                    flowId = item.flowId;
                    activities.add(item);
                }
                else {
                    if(item.flowId.equals(flowId)) {
                        activities.add(item);
                    }
                    else {
                        break;
                    }
                }
            }
            else {
                if(flowId != null) {
                    break;
                }
            }
        }
        return  activities;
    }

    void checkActivityStatus(WorkflowActivity activity, Long userId)
    {
        if(activity == null) {
            throw new BadRequestException("流程活动不存在");
        }

        if(!activity.userId.equals(userId)) {
            throw new BadRequestException("当前用户不是流程活动的处理人");
        }

        if(activity.status.equals(WorkflowActivity.Status.CopyTo)) {
            throw new BadRequestException("当前流程活动已经由其他人处理");
        }

        if(activity.status.equals(WorkflowActivity.Status.Processed)) {
            throw new BadRequestException("流程活动已处理");
        }

        if(activity.status.equals(WorkflowActivity.Status.Returned)) {
            throw new BadRequestException("流程活动已退回");
        }

        if(activity.status.equals(WorkflowActivity.Status.Recalled)) {
            throw new BadRequestException("流程活动已撤回");
        }
    }

    long newFlowId()
    {
        return redisUtil.incr("global:workflow:flowId", 1);
    }

    List listToUserIds(WorkflowActivity.Context activityContext, WorkflowInstance workflowInstance, WorkflowStep toStep)
    {
        List toUserIds = null;
        if (activityContext != null && !Ext.isNullOrEmpty(activityContext.getToUserIds())) {
            toUserIds = Arrays.asList(activityContext.getToUserIds().split(","));
        }
        else {
            String orgIds = "";
            if(activityContext != null && !Ext.isNullOrEmpty(activityContext.getToOrgIds()))
            {
                orgIds = activityContext.getToOrgIds();
            }
            if (Ext.isNullOrEmpty(orgIds) && workflowInstance != null && workflowInstance.getContext() != null) {
                orgIds = workflowInstance.getContext().getOrgIds();
            }
            Long roleId = toStep.roleId;
            if (Ext.isNullOrEmpty(orgIds)) {
                toUserIds = workflowActivityDao.listUserIds(roleId);
            } else {
                toUserIds = workflowActivityDao.listOrgUserIds(roleId, orgIds);
            }
        }
        return toUserIds;
    }

    void listUsers(List userIds, JSONArray users) throws Exception
    {
        for (Object userid : userIds)
        {
            SysUser sysUser = sysUserService.get(Ext.toLong(userid));
            JSONObject user = new JSONObject();
            user.put("codeValue", sysUser.id);
            user.put("codeName", sysUser.userName);
            users.add(user);
        }
    }

    boolean isCountersignActivityAllProcessed(WorkflowActivity activity)
    {
        List<WorkflowActivity> otherActivitys = workflowActivityDao.listOtherActivity(activity.instanceId, activity.flowId, activity.id);
        boolean allProcessed = true;
        for(WorkflowActivity otherActivity : otherActivitys)
        {
            if(!otherActivity.status.equals(WorkflowActivity.Status.Processed))
            {
                allProcessed = false;
                break;
            }
        }
        return allProcessed;
    }
}
