package com.jianli.sys.workflow.dao;


import com.jianli.sys.workflow.domain.WorkflowStepRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface WorkflowStepRelationDao extends Mapper<WorkflowStepRelation>, MySqlMapper<WorkflowStepRelation> {

    @Select("SELECT * FROM workflow_step_relation WHERE workflowId = #{workflowId} AND fromStepId = #{fromStepId} AND deleted = 0 ORDER BY id")
    public List<WorkflowStepRelation> listByFromStep(@Param("workflowId") Long workflowId, @Param("fromStepId") Long fromStepId);

    @Select("SELECT * FROM workflow_step_relation WHERE workflowId = #{workflowId} AND deleted = 0 ORDER BY id")
    public List<WorkflowStepRelation> list(@Param("workflowId") Long workflowId);

    @Select("SELECT workflow_step_relation.* FROM workflow_step_relation LEFT JOIN workflow_step ON workflow_step.id = workflow_step_relation.toStepId WHERE workflowId = #{workflowId} AND workflow_step.stepType = 1 AND deleted = 0 ORDER BY id")
    public WorkflowStepRelation getStart(@Param("workflowId") Long workflowId);
}
