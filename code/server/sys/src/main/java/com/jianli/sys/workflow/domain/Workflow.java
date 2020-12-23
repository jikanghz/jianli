package com.jianli.sys.workflow.domain;

import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="workflow")
public class Workflow extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="流程名称")
    @Column(name="workflowName")
    public String workflowName;

    @Display(name="流程实例页面")
    @Column(name="instanceUrl")
    public String instanceUrl;

    @Display(name="流程实例通知对象")
    @Column(name="instanceNotice")
    public String instanceNotice;

    @Display(name="流程活动通知对象")
    @Column(name="activityNotice")
    public String activityNotice;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;
}
