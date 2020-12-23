package com.jianli.sys.workflow.dao;

import com.jianli.sys.workflow.domain.WorkflowInstance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;

public interface WorkflowInstanceDao extends Mapper<WorkflowInstance>, MySqlMapper<WorkflowInstance> {

    @Select("SELECT * FROM workflow_instance WHERE workflowId = #{workflowId} AND entityId = #{entityId} AND deleted = 0 AND status=1")
    public  WorkflowInstance get(@Param("entityId") Long entityId, @Param("workflowId") Long workflowId);

    @Select("SELECT workflow.instanceUrl, workflow_instance.id as instanceId, workflow_instance.createBy AS workflowCreateBy, userCreateBy.userName AS workflowCreateByName, workflow_instance.createTime AS workFlowCreateTime, workflow_instance.finishTime AS workFlowFinishTime, workflow_instance.status AS instanceStatus, workflow_step.stepName, GROUP_CONCAT(DISTINCT sys_user.userName) AS activityUsers FROM workflow_instance LEFT JOIN workflow_activity ON workflow_instance.id = workflow_activity.instanceId AND workflow_instance.flowId = workflow_activity.flowId LEFT JOIN workflow_step ON workflow_step.id = workflow_activity.stepId LEFT JOIN sys_user ON sys_user.id = workflow_activity.userId LEFT JOIN sys_user userCreateBy ON userCreateBy.id = workflow_instance.createBy LEFT JOIN workflow ON workflow.id = workflow_instance.workflowId WHERE workflow_instance.entityId = #{entityId}  AND workflow_instance.workflowId = (SELECT id FROM workflow WHERE workflowName = #{workflowName} ORDER BY id DESC limit 1) GROUP BY workflow_instance.id, workflow_instance.status, workflow_step.stepName")
    public LinkedHashMap<String, Object> getStatus(@Param("entityId") Long entityId, @Param("workflowName") String workflowName);

}