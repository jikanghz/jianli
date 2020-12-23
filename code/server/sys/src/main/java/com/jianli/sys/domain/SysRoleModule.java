package com.jianli.sys.domain;


import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="sys_role_module")
public class SysRoleModule extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="角色模块Id")
    @Column(name="id")
    public Long id;

    @Required
    @Display(name="角色id")
    @Column(name="roleId")
    public Long roleId;

    @Required
    @Display(name="模块id")
    @Column(name="moduleId")
    public Long moduleId;

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}