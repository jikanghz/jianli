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
import com.jianli.sys.dao.SysRoleDao;
import com.jianli.sys.service.SysCodeService;
import com.jianli.sys.workflow.dao.WorkflowDao;
import com.jianli.sys.workflow.dao.WorkflowStepDao;
import com.jianli.sys.workflow.dao.WorkflowStepRelationDao;
import com.jianli.sys.workflow.domain.Workflow;
import com.jianli.sys.workflow.domain.WorkflowStep;
import com.jianli.sys.workflow.domain.WorkflowStepRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Service("workflowConfig")
public class WorkflowConfigService extends BaseService {

    @Autowired
    private DaoUtil daoUtil;

    @Autowired
    private WorkflowDao workflowDao;

    @Autowired
    private WorkflowStepService workflowStepService;

    @Autowired
    private WorkflowStepDao workflowStepDao;

    @Autowired
    private WorkflowStepRelationDao workflowStepRelationDao;

    @Autowired
    private WorkflowStepRelationService workflowStepRelationService;

    @Autowired
    private SysCodeService sysCodeService;

    @Autowired
    private SysRoleDao sysRoleDao;

    @Resource
    private RedisUtil redisUtil;

    @Autowired
    private SecurityUtil securityUtil;



    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);

        List<QueryCondition> conditions = daoUtil.getConditions(jsonRequest.getData());

        JsonResponse response = JsonResponse.create(jsonRequest);

        conditions.add(new QueryCondition("deleted","=","0"));

        conditions.add(new QueryCondition("tenantId", "=", securityUtil.getTenantId(jsonRequest.getToken()).toString(), true));

        JSONObject data = daoUtil.page("SELECT id,workflowName,status,createTime FROM workflow",
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id"));

        response.setData(data);

        return response;
    }

    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest.getToken(), "workflow", "get");

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");

        Workflow entity = get(id);

        JSONObject entityData = entity.toJObject();

        entityData.put("stepList", workflowStepService.list(entity.id));
        entityData.put("stepRelationList", workflowStepRelationService.list(entity.id));
        response.getData().put("entity", entityData);

        response.getData().put("yesNo", sysCodeService.getCodeList("yesNo"));
        response.getData().put("workflowAnchor", sysCodeService.getCodeList("workflowAnchor"));
        response.getData().put("roleList", sysRoleDao.listCodeTable(securityUtil.getTenantId(jsonRequest.getToken())));

        return response;
    }

    public Workflow get(Long id) throws Exception
    {
        Workflow entity = workflowDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("流程不存在");
        }
        return entity;
    }

    public JsonResponse newId(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);
        response.getData().put("id", newId());
        return response;
    }

    public Long newId()
    {
        long id = redisUtil.incr("global:workflow:configId", 1);
        if(id <= 1)
        {
            id = workflowDao.getMaxId();
            id += 1;
            redisUtil.incr("global:workflow:configId", id);
        }
        return id;
    }


    @Transactional
    public JsonResponse set(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest.getToken(), "workflow", "update");

        Long id = jsonRequest.getData().getLong("id");
        if(Ext.isNullOrZero(id))
        {
            throw new BadRequestException("流程id不能为空");
        }

        Workflow workflow = workflowDao.selectByPrimaryKey(id);

        boolean isInsertWorkflow = false;
        if(workflow == null)
        {
            workflow = new Workflow();
            isInsertWorkflow = true;
        }
        workflow.set(jsonRequest.getData());
        workflow.setDefault(jsonRequest.getUserId());

        workflow.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        if(isInsertWorkflow)
        {
            workflowDao.insert(workflow);
        }
        else
        {
            workflowDao.updateByPrimaryKey(workflow);
        }

        if(jsonRequest.getData().getJSONArray("stepList") != null)
        {
            List<WorkflowStep> itemList = createWorkflowStepList(jsonRequest.getData().getJSONArray("stepList"), id, workflow.tenantId, jsonRequest.getUserId());
            List<Long> itemIds = itemList.stream().map(WorkflowStep::getId).collect(Collectors.toList());

            List<WorkflowStep> dbItemList = workflowStepDao.list(id);
            List<Long> dbItemIds = dbItemList.stream().map(WorkflowStep::getId).collect(Collectors.toList());

            List<WorkflowStep> insertItems = itemList.stream().filter(item ->!dbItemIds.contains(item.id)).collect(Collectors.toList());

            List<WorkflowStep> updateItems = dbItemList.stream().filter(item ->itemIds.contains(item.id)).collect(Collectors.toList());

            List<WorkflowStep> deleteItems = dbItemList.stream().filter(item ->!itemIds.contains(item.id)).collect(Collectors.toList());


            for (WorkflowStep item : insertItems)
            {
                workflowStepDao.insert(item);
            }

            for (WorkflowStep item : updateItems)
            {
                WorkflowStep postItem =  itemList.stream().filter(i-> i.id.equals(item.id)).findFirst().get();
                if(postItem == null)
                {
                    throw new BadRequestException("找不到修改对象的步骤源数据");
                }
                item.set(postItem.toJObject());
                item.setDefault(jsonRequest.getUserId());
                workflowStepDao.updateByPrimaryKey(item);
            }

            for (WorkflowStep item : deleteItems)
            {
                item.setDefault(jsonRequest.getUserId());
                item.deleted = true;
                workflowStepDao.updateByPrimaryKey(item);
            }
        }

        if(jsonRequest.getData().getJSONArray("stepRelationList") != null)
        {
            List<WorkflowStepRelation> itemList = createWorkflowStepRelationList(jsonRequest.getData().getJSONArray("stepRelationList"), id);
            List<Long> itemIds = itemList.stream().map(WorkflowStepRelation::getId).collect(Collectors.toList());

            List<WorkflowStepRelation> dbItemList = workflowStepRelationDao.list(id);
            List<Long> dbItemIds = dbItemList.stream().map(WorkflowStepRelation::getId).collect(Collectors.toList());

            List<WorkflowStepRelation> insertItems = itemList.stream().filter(item ->!dbItemIds.contains(item.id)).collect(Collectors.toList());

            List<WorkflowStepRelation> updateItems = dbItemList.stream().filter(item ->itemIds.contains(item.id)).collect(Collectors.toList());

            List<WorkflowStepRelation> deleteItems = dbItemList.stream().filter(item ->!itemIds.contains(item.id)).collect(Collectors.toList());

            for (WorkflowStepRelation item : insertItems)
            {
                workflowStepRelationDao.insert(item);
            }

            for (WorkflowStepRelation item : updateItems)
            {
                WorkflowStepRelation postItem =  itemList.stream().filter(i-> i.id.equals(item.id)).findFirst().get();
                if(postItem == null)
                {
                    throw new BadRequestException("找不到修改对象的步骤关联源数据");
                }
                item.set(postItem.toJObject());

                workflowStepRelationDao.updateByPrimaryKey(item);
            }

            for (WorkflowStepRelation item : deleteItems)
            {
                item.deleted = true;
                workflowStepRelationDao.updateByPrimaryKey(item);
            }
        }

        JsonResponse response = JsonResponse.create(jsonRequest);

        return response;
    }

    private List<WorkflowStep> createWorkflowStepList(JSONArray dataList, Long workflowId, Long tenantId, Long userId) throws Exception {

        List<WorkflowStep> stepList = new LinkedList<WorkflowStep>();

        for (int i = 0; i < dataList.size(); i++) {
            JSONObject data = dataList.getJSONObject(i);
            WorkflowStep step = new WorkflowStep();
            step.setDefault(userId);
            step.tenantId = tenantId;
            step.set(data);
            step.workflowId = workflowId;
            stepList.add(step);
        }

        return  stepList;
    }

    private List<WorkflowStepRelation> createWorkflowStepRelationList(JSONArray dataList, Long workflowId) throws Exception {

        List<WorkflowStepRelation> relationList = new LinkedList<WorkflowStepRelation>();

        for (int i = 0; i < dataList.size(); i++) {
            JSONObject data = dataList.getJSONObject(i);
            WorkflowStepRelation relation = new WorkflowStepRelation();
            relation.set(data);
            relation.workflowId = workflowId;
            relation.deleted = false;
            relationList.add(relation);
        }

        return  relationList;
    }
}
