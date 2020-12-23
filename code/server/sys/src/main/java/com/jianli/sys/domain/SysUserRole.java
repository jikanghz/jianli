package com.jianli.sys.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="sys_user_role")
public class SysUserRole extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="用户角色id")
    @Column(name="id")
    public Long id;

    @Display(name="用户id")
    @Column(name="userId")
    public Long userId;

    @Display(name="角色id")
    @Column(name="roleId")
    public Long roleId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;
}
