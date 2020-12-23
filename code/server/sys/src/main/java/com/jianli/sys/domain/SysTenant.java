package com.jianli.sys.domain;


import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Email;
import com.jianli.common.domain.validate.Length;
import com.jianli.common.domain.validate.Mobile;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="sys_tenant")
public class SysTenant extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="单位id")
    @Column(name="id")
    public Long id;

    @Required
    @Length(length = 64)
    @Display(name="单位名称")
    @Column(name="tenantName")
    public String tenantName;

    @Length(length = 64)
    @Display(name="联系人姓名")
    @Column(name="contactName")
    public String contactName;

    @Mobile
    @Display(name="联系人手机")
    @Column(name="contactMobile")
    public String contactMobile;

    @Email
    @Display(name="联系人邮箱")
    @Column(name="contactEmail")
    public String contactEmail;

    @Display(name="行政区")
    @Column(name="regionCode")
    public String regionCode;


    @Display(name="地址")
    @Column(name="address")
    public String address;

    @Display(name="单位logo")
    @Column(name="logoUrl")
    public String logoUrl ;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark = "";
}
