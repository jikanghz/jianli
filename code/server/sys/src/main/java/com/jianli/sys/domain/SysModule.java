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

@Table(name="sys_module")
public class SysModule  extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Display(name="模块id")
    @Column(name="id")
    public Long id;

    @Required
    @Length(length = 64)
    @Display(name="模块名称")
    @Column(name="moduleName")
    public String moduleName;

    @Required
    @Display(name="模块类型")
    @Column(name="moduleType")
    public Integer moduleType;

    @Display(name="上级Id")
    @Column(name="parentId")
    public Long parentId;


    @Length(length = 128)
    @Display(name="模块Url")
    @Column(name="moduleUrl")
    public String moduleUrl;

    @Length(length = 128)
    @Display(name="图标Url")
    @Column(name="iconUrl")
    public String iconUrl;

    @Length(length = 64)
    @Display(name="业务对象名称")
    @Column(name="service")
    public String service;

    @Length(length = 64)
    @Display(name="业务方法名称")
    @Column(name="method")
    public String method;

    @Required
    @Display(name="序号")
    @Column(name="itemOrder")
    public Integer itemOrder;

    @Length(length = 64)
    @Display(name="标记")
    @Column(name="tag")
    public String tag;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;

}