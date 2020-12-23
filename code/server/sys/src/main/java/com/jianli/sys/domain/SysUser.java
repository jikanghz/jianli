package com.jianli.sys.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.Sensitive;
import com.jianli.common.domain.validate.Email;
import com.jianli.common.domain.validate.Length;
import com.jianli.common.domain.validate.Mobile;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="sys_user")
public class SysUser extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="用户id")
    public Long id;

    @Display(name="用户姓名")
    @Column(name="userName")
    public String userName;

    @Display(name="登录名")
    @Column(name="loginName")
    public String loginName;

    @Display(name="性别")
    @Column(name="gender")
    public Integer gender;

    @Sensitive
    @Display(name="密码")
    @Column(name="password")
    public String password;

    @Mobile
    @Display(name="手机号")
    @Column(name="mobile")
    public String mobile;

    @Email
    @Display(name="邮箱")
    @Column(name="email")
    public String email;

    @Display(name="QQ号")
    @Column(name="qq")
    public String qq;

    @Display(name="行政区")
    @Column(name="regionCode")
    public String regionCode;

    @Display(name="地址")
    @Column(name="address")
    public String address;

    @Display(name="头像Url")
    @Column(name="imageUrl")
    public String imageUrl;

    @Required
    @Display(name="机构id")
    @Column(name="orgId")
    public Long orgId;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;
}
