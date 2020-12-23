package com.jianli.sys.workflow.domain;

import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="workflowVersion")
public class WorkflowVersion extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="流程版本名称")
    @Column(name="versionName")
    public String versionName;

    @Display(name="流程id")
    @Column(name="workflowId")
    public Long workflowId;

    @Display(name="内容")
    @Column(name="content")
    public String content;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;
}
