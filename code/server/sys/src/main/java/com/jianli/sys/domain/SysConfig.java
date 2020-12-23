package com.jianli.sys.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name="sys_config")
public class SysConfig extends BaseDomain {

    @Id
    @Display(name="代码id")
    @Column(name="id")
    @GeneratedValue(generator = "JDBC")
    public Long id;

    @Display(name="代码值")
    @Column(name="codeValue")
    public String codeValue;

    @Display(name="代码名称")
    @Column(name="codeName")
    public String codeName;
}
