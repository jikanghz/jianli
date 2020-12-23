package com.jianli.sys.workflow.service;

import com.alibaba.fastjson.JSONArray;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.sys.workflow.dao.WorkflowDao;
import com.jianli.sys.workflow.dao.WorkflowStepDao;
import com.jianli.sys.workflow.dao.WorkflowStepRelationDao;
import com.jianli.sys.workflow.domain.WorkflowStep;
import com.jianli.sys.workflow.domain.WorkflowStepRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service("workflowStepRelation")
public class WorkflowStepRelationService extends BaseService {

    @Autowired
    WorkflowStepRelationDao workflowStepRelationDao;

    JSONArray list(Long workflowId) throws Exception
    {
        List<WorkflowStepRelation> stepRelations = workflowStepRelationDao.list(workflowId);
        JSONArray items = new JSONArray();
        for (WorkflowStepRelation stepRelation : stepRelations)
        {
            items.add(stepRelation.toJObject());
        }
        return items;
    }

}
