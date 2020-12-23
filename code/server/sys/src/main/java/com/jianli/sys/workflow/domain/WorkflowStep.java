package com.jianli.sys.workflow.domain;

import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="workflow_step")
public class WorkflowStep extends EntityDomain {

    @Id
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="步骤名称")
    @Column(name="stepName")
    public String stepName;

    @Display(name="步骤类型")
    @Column(name="stepType")
    public Integer stepType;

    @Display(name="流程id")
    @Column(name="workflowId")
    public Long workflowId;

    @Display(name="流程活动页面")
    @Column(name="activityUrl")
    public String activityUrl;

    @Display(name="角色id")
    @Column(name="roleId")
    public Long roleId;

    @Display(name="是否允许退回")
    @Column(name="allowReturn")
    public Integer allowReturn;

    @Display(name="是否会签步骤")
    @Column(name="countersign")
    public Integer countersign;

    @Display(name="中心点x坐标")
    @Column(name="x")
    public Integer x;

    @Display(name="中心点y坐标")
    @Column(name="y")
    public Integer y;

    @Display(name="宽度")
    @Column(name="width")
    public Integer width;

    @Display(name="高度")
    @Column(name="height")
    public Integer height;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isCountersign()
    {
        if(countersign != null && countersign.equals(1))
        {
            return true;
        }
        return false;
    }

    public static class Type
    {
        //开始步骤
        public static Integer Start = 1;

        //中间步骤
        public static Integer Intermediate = 2;

        //线束步骤
        public static Integer End = 3;
    }
}
