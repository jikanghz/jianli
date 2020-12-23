package com.jianli.sys.domain;

import com.jianli.common.domain.BaseDomain;
import com.jianli.common.domain.Display;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="sys_code")
public class SysRegion extends BaseDomain {

    @Id
    @Display(name="行政区域id")
    @Column(name="id")
    @GeneratedValue(generator = "JDBC")
    public Long id;

    @Display(name="上级id")
    @Column(name="parentId")
    public String parentId;

    @Display(name="行政区域名称")
    @Column(name="regionName")
    public String regionName;

    @Display(name="层级")
    @Column(name="layer")
    public Integer layer;

    @Display(name="行政区域代码")
    @Column(name="regionCode")
    public Integer itemOrder;

}
