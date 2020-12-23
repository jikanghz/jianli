package com.jianli.business.domain;

import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="project")
public class Project extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="id")
    @Column(name="id")
    public Long id;

    @Display(name="项目编号")
    @Column(name="projectCode")
    public String projectCode;

    @Display(name="项目名称")
    @Column(name="projectName")
    public String projectName;

    @Display(name="项目类型")
    @Column(name="projectType")
    public Integer projectType;

    @Display(name="预算金额")
    @Column(name="budgetAmount")
    public Long budgetAmount;

    @Display(name="预算组成")
    @Column(name="budgetComposition")
    public String budgetComposition;

    @Display(name="申建单位")
    @Column(name="applyOrg")
    public String applyOrg;

    @Display(name="预算描述")
    @Column(name="budgetDesc")
    public String budgetDesc;

    @Display(name="项目描述")
    @Column(name="projectDesc")
    public String projectDesc;

    @Display(name="申报描述")
    @Column(name="applyDesc")
    public String applyDesc;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Display(name="备注")
    @Column(name="remark")
    public String remark;
}
