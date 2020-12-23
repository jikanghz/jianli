package com.jianli.sys.workflow.service;

import com.alibaba.fastjson.JSONArray;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.sys.workflow.dao.WorkflowDao;
import com.jianli.sys.workflow.dao.WorkflowStepDao;
import com.jianli.sys.workflow.dao.WorkflowStepRelationDao;
import com.jianli.sys.workflow.domain.Workflow;
import com.jianli.sys.workflow.domain.WorkflowInstance;
import com.jianli.sys.workflow.domain.WorkflowStep;
import com.jianli.sys.workflow.domain.WorkflowStepRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Service("workflowStep")
public class WorkflowStepService extends BaseService {
    @Autowired
    private WorkflowStepDao workflowStepDao;

    @Autowired
    private WorkflowStepRelationDao workflowStepRelationDao;

    @Autowired
    private WorkflowDao workflowDao;

    public WorkflowStep get(Long id) throws Exception {
        WorkflowStep entity = workflowStepDao.selectByPrimaryKey(id);
        if (entity == null || entity.deleted) {
            throw new BadRequestException("流程步骤不存在");
        }
        return entity;
    }

    WorkflowStep getToStep(Long workflowId, Long fromStepId, String condition) {
        final String finalCondition = condition;
        List<WorkflowStepRelation> toStepRelations = workflowStepRelationDao.listByFromStep(workflowId, fromStepId);
        WorkflowStep toStep = null;
        if (toStepRelations.size() == 1) {
            WorkflowStepRelation toStepRelation = toStepRelations.get(0);
            toStep = workflowStepDao.selectByPrimaryKey(toStepRelation.toStepId);
        } else {
            List<WorkflowStepRelation> stepRelations = toStepRelations.stream().filter(item -> item.stepCondition.equals(finalCondition)).collect(Collectors.toList());
            if (stepRelations.size() < 1) {
                throw new BadRequestException("没有找到下一步所对应的分支");
            }
            if (stepRelations.size() > 1) {
                throw new BadRequestException("下一步有多个分支，不知道走哪条");
            }
            WorkflowStepRelation toStepRelation = stepRelations.get(0);
            toStep = workflowStepDao.selectByPrimaryKey(toStepRelation.toStepId);
        }
        return toStep;
    }

    WorkflowStep getStartToStep(String workflowName, String condition, Long tenantId) {
        Long workflowId = workflowDao.getWorkflowId(workflowName, tenantId);
        WorkflowStep startStep = workflowStepDao.getStartStep(workflowId);
        return getToStep(workflowId, startStep.id, condition);
    }

    JSONArray list(Long workflowId) throws Exception
    {
        List<WorkflowStep> steps = workflowStepDao.list(workflowId);
        JSONArray items = new JSONArray();
        for (WorkflowStep step : steps)
        {
            items.add(step.toJObject());
        }
        return items;
    }

}
