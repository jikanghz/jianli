package com.jianli.sys.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;
import com.jianli.common.domain.validate.Mobile;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Table(name="sys_org")
public class SysOrg extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="机构id")
    @Column(name="id")
    public Long id;

    @Required
    @Length(length = 64)
    @Display(name="机构名称")
    @Column(name="orgName")
    public String orgName;

    @Display(name="机构类型")
    @Column(name="orgType")
    public Integer orgType;

    @Display(name="上级Id")
    @Column(name="parentId")
    public Long parentId;

    @Display(name="路径")
    @Column(name="path")
    public String path;

    @Display(name="行政区")
    @Column(name="regionCode")
    public String regionCode;

    @Display(name="地址")
    @Column(name="address")
    public String address;

    @Length(length = 64)
    @Display(name="负责人姓名")
    @Column(name="contactName")
    public String contactName;

    @Mobile
    @Display(name="联系电话")
    @Column(name="contactMobile")
    public String contactMobile;

    @Display(name="负责人照片")
    @Column(name="contactUrl")
    public String contactUrl ;


    @Display(name="序号")
    @Column(name="itemOrder")
    public Integer itemOrder;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;

    public void SetPath(String parentPath)
    {
        path = parentPath + id + ",";
    }
}
