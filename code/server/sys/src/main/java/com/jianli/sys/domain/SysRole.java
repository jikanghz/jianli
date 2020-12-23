package com.jianli.sys.domain;


import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="sys_role")
public class SysRole extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="角色id")
    @Column(name="id")
    public Long id;

    @Required
    @Length(length = 64)
    @Display(name="角色名称")
    @Column(name="roleName")
    public String roleName;


    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;

}