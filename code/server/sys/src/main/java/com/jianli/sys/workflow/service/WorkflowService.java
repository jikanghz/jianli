package com.jianli.sys.workflow.service;

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
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.domain.SysRole;
import com.jianli.sys.domain.SysUser;
import com.jianli.sys.domain.SysUserRole;
import com.jianli.sys.workflow.dao.WorkflowDao;
import com.jianli.sys.workflow.dao.WorkflowStepDao;
import com.jianli.sys.workflow.domain.Workflow;
import com.jianli.sys.workflow.domain.WorkflowStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;


@Service("workflow")
public class WorkflowService extends BaseService {
    @Autowired
    private WorkflowDao workflowDao;

    @Autowired
    private WorkflowStepDao workflowStepDao;

    @Autowired
    private WorkflowConfigService workflowConfigService;

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

        JSONObject data = daoUtil.page("SELECT * FROM workflow",
                conditions,  daoUtil.getPageInfo(jsonRequest.getData(), "id DESC"));

        response.setData(data);
        return response;
    }

    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Workflow entity = null;
        if(Ext.isNullOrZero(id))
        {
            entity = new Workflow();
        }
        else
        {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();
        response.getData().put("entity", entityData);
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

    public Long getWorkflowId(String workflowName, Long tenantId)
    {
        return workflowDao.getWorkflowId(workflowName, tenantId);
    }

    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Workflow entity = new Workflow();

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        entity.tenantId = securityUtil.getTenantId(jsonRequest.getToken());

        entity.id = workflowConfigService.newId();

        workflowDao.insert(entity);

        WorkflowStep step = new WorkflowStep();
        step.setDefault(jsonRequest.getUserId());
        step.id = workflowConfigService.newId();
        step.tenantId =   entity.tenantId;
        step.workflowId = entity.id;
        step.stepType = WorkflowStep.Type.Start;
        step.stepName = "开始";
        step.x = 360;
        step.y = 80;
        workflowStepDao.insert(step);

        step = new WorkflowStep();
        step.setDefault(jsonRequest.getUserId());
        step.id = workflowConfigService.newId();
        step.tenantId =   entity.tenantId;
        step.workflowId = entity.id;
        step.stepType = WorkflowStep.Type.End;
        step.stepName = "结束";
        step.x = 360;
        step.y = 560;
        workflowStepDao.insert(step);

        return response;
    }

    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Workflow entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        workflowDao.updateByPrimaryKey(entity);

        return response;
    }

    @Transactional
    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        Workflow entity = get(id);

        entity.setDefault(jsonRequest.getUserId());
        entity.deleted = true;
        workflowDao.updateByPrimaryKey(entity);

        return response;
    }
}
