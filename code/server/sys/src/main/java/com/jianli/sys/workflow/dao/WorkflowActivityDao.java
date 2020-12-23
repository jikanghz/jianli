package com.jianli.sys.workflow.dao;

import com.jianli.sys.workflow.domain.WorkflowActivity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;

public interface WorkflowActivityDao extends Mapper<WorkflowActivity>, MySqlMapper<WorkflowActivity> {

    @Select("SELECT * FROM workflow_activity WHERE instanceId = #{instanceId} AND flowId = #{flowId} AND id <> #{id} AND deleted = 0 ORDER BY id")
    public List<WorkflowActivity> listOtherActivity(@Param("instanceId") Long instanceId, @Param("flowId") Long flowId, @Param("id") Long id);

    @Select("SELECT * FROM workflow_activity WHERE instanceId = #{instanceId} AND deleted = 0 ORDER BY id DESC")
    public List<WorkflowActivity> list(@Param("instanceId") Long instanceId);

    @Select("SELECT workflow_activity.id, workflow_step.stepName, workflow_step.stepType, workflow_step.countersign, workflow_activity.createTime, sys_user.userName AS activityUser, workflow_activity.status, workflow_activity.remark,workflow_activity.fromStepId, workflow_activity.stepId FROM workflow_activity LEFT JOIN workflow_step ON workflow_step.id = workflow_activity.stepId  LEFT JOIN sys_user ON sys_user.id = workflow_activity.userId WHERE instanceId = (SELECT id FROM workflow_instance WHERE  entityId = #{entityId} AND workflowId = (SELECT id FROM workflow WHERE workflowName = #{workflowName} ORDER BY id DESC limit 1)) ORDER BY workflow_activity.flowId DESC, workflow_activity.status ASC")
    public List<LinkedHashMap<String, Object>> listActivity(@Param("entityId") Long entityId, @Param("workflowName") String workflowName);

    @Select("SELECT id FROM sys_user WHERE id IN (SELECT userId FROM sys_user_role WHERE roleId = #{roleId}) ORDER BY id")
    public List<Long> listUserIds(@Param("roleId") Long roleId);

    @Select("SELECT id FROM sys_user WHERE id IN (SELECT userId FROM sys_user_role WHERE roleId = #{roleId}) AND orgId IN (${orgIds}) ORDER BY id")
    public List<Long> listOrgUserIds(@Param("roleId") Long roleId, @Param("orgIds") String orgIds);

}