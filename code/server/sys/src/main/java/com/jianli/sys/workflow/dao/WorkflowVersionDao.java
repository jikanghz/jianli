package com.jianli.sys.workflow.dao;

import com.jianli.sys.workflow.domain.WorkflowVersion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;

public interface WorkflowVersionDao extends Mapper<WorkflowVersion>, MySqlMapper<WorkflowVersion> {

    @Select("SELECT * FROM workflow_version WHERE workflowId = #{workflowId} AND deleted = 0 ORDER BY id DESC")
    public List<WorkflowVersion> list(@Param("workflowId") Long workflowId);

}