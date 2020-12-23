package com.jianli.sys.workflow.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.redis.RedisUtil;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.service.SysUserService;
import com.jianli.sys.workflow.dao.*;
import com.jianli.sys.workflow.domain.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service("workflowEngine")
public class WorkflowEngine extends BaseService {

    @Autowired
    private WorkflowDao workflowDao;

    @Autowired
    private WorkflowStepDao workflowStepDao;

    @Autowired
    private WorkflowStepRelationDao workflowStepRelationDao;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private WorkflowActivityDao workflowActivityDao;

    @Autowired
    private WorkflowActivityService workflowActivityService;

    @Autowired
    private WorkflowStepService workflowStepService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private BeanFactory beanFactory;

    @Transactional
    public void start(String workflowName, Long entityId, String instanceCode, WorkflowInstance.Context instanceContent, WorkflowActivity.Context activityContext, Long userId, Long tenantId) throws Exception {
        Long workflowId = workflowService.getWorkflowId(workflowName, tenantId);

        Workflow workflow = workflowService.get(workflowId);
        if (workflow.status != 1) {
            throw new BadRequestException("流程未启用");
        }

        WorkflowInstance workflowInstance = workflowInstanceService.get(entityId, workflowId);
        if (workflowInstance != null) {
            throw new BadRequestException("流程已运行，请勿重复创建");
        }

        WorkflowStep startStep = workflowStepDao.getStartStep(workflowId);
        if (startStep == null) {
            throw new BadRequestException("流程未配置开始步骤");
        }

        long flowId = workflowActivityService.newFlowId();

        workflowInstance = new WorkflowInstance();
        workflowInstance.workflowId = workflowId;
        workflowInstance.entityId = entityId;
        workflowInstance.instanceCode = instanceCode;
        workflowInstance.tenantId = workflow.tenantId;
        workflowInstance.flowId = flowId;
        workflowInstance.setContext(instanceContent);
        workflowInstance.setDefault(userId);
        workflowInstanceDao.insert(workflowInstance);

        if (!Ext.isNullOrEmpty(workflow.instanceNotice)) {
            IWorkflowInstanceNotice instanceNotice = (IWorkflowInstanceNotice) beanFactory.getBean(workflow.instanceNotice);
            if (instanceNotice != null) {
                instanceNotice.onStarted(workflowInstance, userId);
            }
        }

        WorkflowActivity activity = workflowActivityService.newActivity(workflowId, workflowInstance.id, instanceCode,
                0L, startStep.id, flowId, userId, workflowInstance.tenantId, activityContext);

        workflowActivityDao.insert(activity);

        process(activity, activityContext, userId, "");
    }

    @Transactional
    public void receive(Long activityId, WorkflowActivity.Context activityContext, Long userId) throws Exception {
        WorkflowActivity activity = workflowActivityService.get(activityId);
        if (!activity.userId.equals(userId)) {
            throw new BadRequestException("当前用户不是流程活动的处理人");
        }

        if (activity.status.equals(WorkflowActivity.Status.NotReceived)) {
            activity.status = WorkflowActivity.Status.Received;
            activity.receiveTime = Ext.now();
            activity.setDefault(userId);

            workflowActivityDao.updateByPrimaryKey(activity);

            WorkflowStep step = workflowStepDao.selectByPrimaryKey(activity.stepId);
            if (!step.isCountersign()) {
                List<WorkflowActivity> otherActivities = workflowActivityDao.listOtherActivity(activity.instanceId, activity.flowId, activity.id);
                for (WorkflowActivity otherActivity : otherActivities) {
                    otherActivity.status = WorkflowActivity.Status.CopyTo;
                    workflowActivityDao.updateByPrimaryKey(otherActivity);
                }
            }
        }
    }

    @Transactional
    public void process(Long activityId, WorkflowActivity.Context activityContext, Long userId, String remark) throws Exception {
        WorkflowActivity activity = workflowActivityDao.selectByPrimaryKey(activityId);
        process(activity, activityContext, userId, remark);
    }

    @Transactional
    public void process(WorkflowActivity activity, WorkflowActivity.Context activityContext, Long userId, String remark) throws Exception {
        workflowActivityService.checkActivityStatus(activity, userId);

        Workflow workflow = workflowService.get(activity.workflowId);
        WorkflowStep step = workflowStepService.get(activity.stepId);

        activity.remark = remark;
        activity.status = WorkflowActivity.Status.Processed;
        activity.processTime = Ext.now();
        workflowActivityDao.updateByPrimaryKey(activity);

        if (step.countersign.equals(1)) {
            if (!workflowActivityService.isCountersignActivityAllProcessed(activity)) {
                return;
            }
        }

        String condition = "";
        if (activityContext != null && !Ext.isNullOrEmpty(activityContext.getCondition())) {
            condition = activityContext.getCondition();
        }

        WorkflowStep toStep = workflowStepService.getToStep(activity.workflowId, activity.stepId, condition);

        long flowId = workflowActivityService.newFlowId();

        WorkflowInstance workflowInstance = workflowInstanceService.get(activity.instanceId);
        if (toStep.stepType.equals(WorkflowStep.Type.Intermediate)) {
            List toUserIds = workflowActivityService.listToUserIds(activityContext, workflowInstance, toStep);
            if(toUserIds.size() < 1)
            {
                throw new BadRequestException("找不到流程下一步的接收人");
            }
            for (Object toUserId : toUserIds) {
                WorkflowActivity toActivity = workflowActivityService.newActivity(activity.workflowId, activity.instanceId, activity.instanceCode,
                        activity.stepId, toStep.id, flowId, Ext.toLong(toUserId), activity.tenantId, activityContext);
                workflowActivityDao.insert(toActivity);
                if (!Ext.isNullOrEmpty(workflow.activityNotice)) {
                    IWorkflowActivityNotice activityNotice = (IWorkflowActivityNotice) beanFactory.getBean(workflow.activityNotice);
                    if (activityNotice != null) {
                        activityNotice.onProcessed(workflowInstance, activity, toActivity, userId);
                    }
                }
            }
            workflowInstance.flowId = flowId;
            workflowInstanceDao.updateByPrimaryKey(workflowInstance);
        } else if (toStep.stepType.equals(WorkflowStep.Type.End)) {
            WorkflowActivity newActivity = workflowActivityService.newActivity(activity.workflowId, activity.instanceId, activity.instanceCode,
                    activity.stepId, toStep.id, flowId, userId, activity.tenantId, activityContext);
            newActivity.status = WorkflowActivity.Status.Processed;
            workflowActivityDao.insert(newActivity);

            workflowInstance.flowId = flowId;
            workflowInstance.status = WorkflowInstance.Status.Completed;
            workflowInstance.finishBy = userId;
            workflowInstance.finishTime = Ext.now();

            workflowInstanceDao.updateByPrimaryKey(workflowInstance);

            if (!Ext.isNullOrEmpty(workflow.instanceNotice)) {
                IWorkflowInstanceNotice instanceNotice = (IWorkflowInstanceNotice) beanFactory.getBean(workflow.instanceNotice);
                if (instanceNotice != null) {
                    instanceNotice.onCompleted(workflowInstance, userId);
                }
            }
        }
    }

    @Transactional
    public void returnTo(Long activityId, WorkflowActivity.Context activityContext, Long userId, String remark) throws Exception {
        WorkflowActivity activity = workflowActivityDao.selectByPrimaryKey(activityId);

        workflowActivityService.checkActivityStatus(activity, userId);

        Long returnTo = null;
        if (activityContext != null) {
            returnTo = activityContext.getReturnTo();
        }
        if (returnTo == null) {
            throw new BadRequestException("未指定退回步骤");
        }

        WorkflowStep toStep = workflowStepService.get(returnTo);
        if (!toStep.workflowId.equals(activity.workflowId)) {
            throw new BadRequestException("退回步骤不在同一个流程中");
        }

        Workflow workflow = workflowService.get(activity.workflowId);

        WorkflowInstance workflowInstance = workflowInstanceService.get(activity.instanceId);

        activity.status = WorkflowActivity.Status.Returned;
        activity.returnTime = Ext.now();
        activity.remark = remark;
        workflowActivityDao.updateByPrimaryKey(activity);

        WorkflowStep step = workflowStepService.get(activity.stepId);
        if (step.countersign.equals(1)) {
            List<WorkflowActivity> otherActivitys = workflowActivityDao.listOtherActivity(activity.instanceId, activity.flowId, activity.id);
            for (WorkflowActivity otherActivity : otherActivitys) {
                if (otherActivity.status.equals(WorkflowActivity.Status.NotReceived) || otherActivity.status.equals(WorkflowActivity.Status.Received)) {
                    otherActivity.status = WorkflowActivity.Status.Returned;
                    otherActivity.returnTime = Ext.now();
                    workflowActivityDao.updateByPrimaryKey(otherActivity);
                }
            }

        }

        List<WorkflowActivity> activities = workflowActivityService.listActivityByStepId(activity.instanceId, returnTo);
        if (activities.size() < 1) {
            throw new BadRequestException("流程没有经过该退回步骤");
        }
        long flowId = workflowActivityService.newFlowId();
        for (WorkflowActivity oldActivity : activities) {
            WorkflowActivity toActivity = workflowActivityService.newActivity(activity.workflowId, activity.instanceId, activity.instanceCode,
                    activity.stepId, toStep.id, flowId, oldActivity.userId, activity.tenantId, activityContext);
            workflowActivityDao.insert(toActivity);

            if (!Ext.isNullOrEmpty(workflow.activityNotice)) {
                IWorkflowActivityNotice activityNotice = (IWorkflowActivityNotice) beanFactory.getBean(workflow.activityNotice);
                if (activityNotice != null) {
                    activityNotice.onReturned(workflowInstance, activity, toActivity, userId);
                }
            }
        }


        workflowInstance.flowId = flowId;
        workflowInstanceDao.updateByPrimaryKey(workflowInstance);
    }


    //set method
    public void setWorkflowInstanceContext(Long instanceId, WorkflowInstance.Context content) {
        WorkflowInstance workflowInstance = workflowInstanceDao.selectByPrimaryKey(instanceId);
        workflowInstance.setContext(content);

        workflowInstanceDao.updateByPrimaryKey(workflowInstance);
    }


    //get method

    public WorkflowInstance getWorkflowInstance(Long instanceId) throws Exception {
        return workflowInstanceService.get(instanceId);
    }

    public WorkflowActivity getWorkflowActivity(Long activityId) throws Exception {
        return workflowActivityService.get(activityId);
    }

    public void getInstanceStatus(JSONObject entity, Long entityId, String workflowName) throws Exception {
        JSONObject workflowStatus = getInstanceStatus(entityId, workflowName);
        entity.put("instanceUrl", workflowStatus.getString("instanceUrl"));
        entity.put("instanceId", workflowStatus.getString("instanceId"));
        entity.put("workflowCreateBy", workflowStatus.getLong("workflowCreateBy"));
        entity.put("workflowCreateByName", workflowStatus.getString("workflowCreateByName"));
        entity.put("workFlowCreateTime", Ext.toDateString(workflowStatus.getString("workFlowCreateTime"), "yyyy-MM-dd HH:mm:ss"));
        entity.put("workFlowFinishTime", Ext.toDateString(workflowStatus.getString("workFlowFinishTime"), "yyyy-MM-dd HH:mm:ss"));
        entity.put("instanceStatusName", workflowStatus.getString("instanceStatusName"));
        entity.put("stepName", workflowStatus.getString("stepName"));
        if (workflowStatus.getInteger("instanceStatus") != null && !workflowStatus.getInteger("instanceStatus").equals(WorkflowInstance.Status.Completed)) {
            entity.put("activityUsers", workflowStatus.getString("activityUsers"));
        }
    }

    public JSONObject getInstanceStatus(Long entityId, String workflowName) {
        JSONObject data = Ext.toJObject(workflowInstanceDao.getStatus(entityId, workflowName));
        data.put("instanceStatusName", WorkflowInstance.Status.getStatusName(data.getInteger("instanceStatus")));
        return data;
    }

    public JSONArray listActivity(Long entityId, String workflowName) throws Exception {
        JSONArray activities = Ext.toJArray(workflowActivityDao.listActivity(entityId, workflowName));
        for (int i = 0; i < activities.size(); i++) {
            JSONObject activity = activities.getJSONObject(i);
            activity.put("statusName", WorkflowActivity.Status.getStatusName(activity.getInteger("status")));
            activity.put("createTime", Ext.toDateString(activity.getString("createTime"), "yyyy-MM-dd HH:mm:ss"));

            String remark = "";
            if (!Ext.isNullOrEmpty(activity.getString("remark"))) {
                remark = activity.getString("remark");
            }

            if (activity.getInteger("stepType").equals(WorkflowStep.Type.Intermediate)) {
                activity.put("content", activity.getString("stepName") + " " + activity.getString("activityUser") + " " + activity.getString("statusName") + " " + remark);
            } else {
                activity.put("content", activity.getString("stepName") + " " + activity.getString("activityUser") + " " + remark);
            }

            Integer status = activity.getInteger("status");
            if (status.equals(1) || status.equals(2)) {
                activity.put("color", "#0bbd87");
            }

            Integer countersign = activity.getInteger("countersign");

            if (countersign != null && countersign.equals(1)) {
                activity.put("color", "#f48441");
            }
        }
        return activities;
    }

    public JSONArray listReturnTo(JSONArray activities, Long activityId) {
        JSONArray returnTos = new JSONArray();
        JSONObject activity = Ext.find(activities, "id", activityId);
        if (activity != null) {
            int i = 50;
            while (--i >= 0) {
                JSONObject from = Ext.find(activities, "stepId", activity.getLong("fromStepId"));
                if (from != null) {
                    JSONObject returnTo = new JSONObject(true);
                    returnTo.put("codeValue", from.getLong("stepId"));
                    returnTo.put("codeName", from.getString("stepName"));
                    returnTos.add(returnTo);
                    activity = from;
                } else {
                    break;
                }
                if (Ext.isNullOrZero(from.getLong("fromStepId")) || from.getInteger("stepType").equals(WorkflowStep.Type.Start)) {
                    break;
                }
            }
        }
        return returnTos;
    }

    public JsonResponse listStartNextCountersignUsers(JsonRequest jsonRequest) throws Exception {
        String workflowName = jsonRequest.getData().getString("workflowName");
        WorkflowActivity.Context activityContext = new WorkflowActivity.Context();
        activityContext.set(jsonRequest.getData());
        Long tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        JsonResponse response = JsonResponse.create(jsonRequest);
        response.getData().put("countersignUsers", listStartNextCountersignUsers(workflowName, activityContext, tenantId));
        return response;
    }

    public JSONArray listStartNextCountersignUsers(String workflowName, WorkflowActivity.Context activityContext, Long tenantId) throws Exception {
        JSONArray users = new JSONArray();
        String condition = "";
        if (activityContext != null && !Ext.isNullOrEmpty(activityContext.getCondition())) {
            condition = activityContext.getCondition();
        }
        WorkflowStep toStep = workflowStepService.getStartToStep(workflowName, condition, tenantId);
        if (toStep != null && toStep.isCountersign()) {
            List userIds = workflowActivityService.listToUserIds(activityContext, null, toStep);
            workflowActivityService.listUsers(userIds, users);
        }
        return users;
    }

    public JsonResponse listNextCountersignUsers(JsonRequest jsonRequest) throws Exception {
        Long activityId = jsonRequest.getData().getLong("activityId");
        WorkflowActivity.Context activityContext = new WorkflowActivity.Context();
        activityContext.set(jsonRequest.getData());

        JsonResponse response = JsonResponse.create(jsonRequest);
        response.getData().put("countersignUsers", listNextCountersignUsers(activityId, activityContext));
        return response;
    }

    public JSONArray listNextCountersignUsers(Long activityId, WorkflowActivity.Context activityContext) throws Exception {
        JSONArray users = new JSONArray();
        WorkflowActivity activity = workflowActivityDao.selectByPrimaryKey(activityId);
        WorkflowInstance workflowInstance = workflowInstanceDao.selectByPrimaryKey(activity.instanceId);
        String condition = "";
        if (activityContext != null && !Ext.isNullOrEmpty(activityContext.getCondition())) {
            condition = activityContext.getCondition();
        }
        WorkflowStep toStep = workflowStepService.getToStep(activity.workflowId, activity.stepId, condition);
        if (toStep.isCountersign()) {
            List userIds = workflowActivityService.listToUserIds(activityContext, workflowInstance, toStep);
            workflowActivityService.listUsers(userIds, users);
        }
        return users;
    }
}
