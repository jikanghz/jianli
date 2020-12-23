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

@Table(name="sys_code")
public class SysCode extends EntityDomain {

    @Id
    @Display(name="代码id")
    @Column(name="id")
    @GeneratedValue(generator = "JDBC")
    public Long id;

    @Display(name="代码值")
    @Column(name="codeValue")
    public Integer codeValue;

    @Display(name="代码名称")
    @Column(name="codeName")
    public String codeName;

    @Display(name="代码分类")
    @Column(name="codeCategory")
    public String codeCategory;

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
}
