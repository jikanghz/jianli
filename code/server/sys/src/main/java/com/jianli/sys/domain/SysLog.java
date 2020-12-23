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

@Table(name="sys_log")
public class SysLog extends BaseDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="服务名")
    @Column(name="service")
    public String service;

    @Display(name="方法名")
    @Column(name="method")
    public String method;

    @Display(name="请求数据")
    @Column(name="request")
    public String request;

    @Display(name="响应数据")
    @Column(name="response")
    public String response;

    @Display(name="ip地址")
    @Column(name="ip")
    public String ip;

    @Display(name="时长")
    @Column(name="duration")
    public Long duration;

    @Display(name="新建人")
    @Column(name="createBy")
    public Long createBy;

    @Display(name="新建时间")
    @Column(name="createTime")
    public Date createTime;
}
