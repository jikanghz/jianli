package com.jianli.common.domain;

import com.jianli.common.Ext;
import com.jianli.common.domain.validate.Required;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

public class EntityDomain extends BaseDomain  {

    @Sensitive
    @Display(name="是否删除")
    @Column(name="deleted")
    public Boolean deleted;

    @Display(name="状态")
    @Column(name="status")
    public Integer status;

    @Display(name="新建人")
    @Column(name="createBy")
    public Long createBy;

    @Display(name="新建时间")
    @Column(name="createTime")
    public Date createTime;

    @Display(name="修改人")
    @Column(name="updateBy")
    public Long updateBy;

    @Display(name="修改时间")
    @Column(name="updateTime")
    public Date updateTime;

    public void setDefault(Long userId)
    {
        if (createTime == null)
        {
            deleted = false;
            status = 1;
            createBy = userId;
            createTime = Ext.now();
        }
        updateBy = userId;
        updateTime = Ext.now();
    }
}
