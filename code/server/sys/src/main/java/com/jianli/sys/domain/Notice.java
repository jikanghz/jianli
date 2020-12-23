package com.jianli.sys.domain;

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

@Table(name="notice")
public class Notice extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Length(length = 128)
    @Display(name="标题")
    @Column(name="title")
    public String title;

    @Display(name="内容")
    @Column(name="content")
    public String content;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Column(name="remark")
    @Display(name="备注")
    public String remark;
}
