package com.jianli.sys.workflow.dao;

import com.jianli.sys.workflow.domain.WorkflowStep;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface WorkflowStepDao extends Mapper<WorkflowStep>, MySqlMapper<WorkflowStep> {

    @Select("SELECT * FROM workflow_step WHERE workflowId = #{workflowId} AND deleted = 0 ORDER BY id")
    public List<WorkflowStep> list(@Param("workflowId") Long workflowId);

    @Select("SELECT * FROM workflow_step WHERE workflowId = #{workflowId} AND stepType = 1 AND deleted = 0 ORDER BY id DESC LIMIT 1")
    public  WorkflowStep getStartStep(@Param("workflowId") Long workflowId);
}
