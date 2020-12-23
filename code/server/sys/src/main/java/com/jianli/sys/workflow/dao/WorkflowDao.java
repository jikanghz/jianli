package com.jianli.sys.workflow.dao;

import com.jianli.sys.workflow.domain.Workflow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface WorkflowDao extends Mapper<Workflow>, MySqlMapper<Workflow> {
    @Select("SELECT id FROM workflow WHERE workflowName = #{workflowName} AND tenantId = #{tenantId} ORDER BY id DESC limit 1")
    public Long getWorkflowId(@Param("workflowName") String workflowName, @Param("tenantId") Long tenantId);

    @Select("SELECT MAX(id) id FROM (SELECT MAX(id) AS id FROM workflow UNION ALL SELECT MAX(id) AS id FROM workflow_step UNION ALL SELECT MAX(id) AS id FROM workflow_step_relation ) T")
    public Long getMaxId();
}