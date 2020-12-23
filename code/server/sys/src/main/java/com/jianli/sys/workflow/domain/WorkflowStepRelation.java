package com.jianli.sys.workflow.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.Sensitive;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="workflow_step_relation")
public class WorkflowStepRelation extends BaseDomain {

    @Id
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="流程id")
    @Column(name="workflowId")
    public Long workflowId;

    @Display(name="前步骤id")
    @Column(name="fromStepId")
    public Long fromStepId;

    @Display(name="前步骤锚点")
    @Column(name="fromStepAnchor")
    public Long fromStepAnchor;

    @Display(name="后步骤id")
    @Column(name="toStepId")
    public Long toStepId;

    @Display(name="后步骤锚点")
    @Column(name="toStepAnchor")
    public Long toStepAnchor;

    @Display(name="条件")
    @Column(name="stepCondition")
    public String stepCondition;

    @Sensitive
    @Display(name="是否删除")
    @Column(name="deleted")
    public Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
