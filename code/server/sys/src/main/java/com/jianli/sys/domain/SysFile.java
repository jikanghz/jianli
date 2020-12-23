package com.jianli.sys.domain;

import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "sys_file")
public class SysFile extends EntityDomain {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Length(length = 64)
    @Column(name="fileName")
    @Display(name="文件名")
    public String fileName;

    @Length(length = 128)
    @Column(name="filePath")
    @Display(name="文件路径")
    public String filePath;

    @Length(length = 256)
    @Column(name="fileUrl")
    @Display(name="文件url")
    public String fileUrl;

    @Length(length = 32)
    @Column(name="fileSuffix")
    @Display(name="文件后缀名")
    public String fileSuffix;

    @Length(length = 64)
    @Column(name="entity")
    @Display(name="实体名称")
    public String entity;

    @Length(length = 64)
    @Column(name="entityId")
    @Display(name="实体id")
    public String entityId;

    @Length(length = 64)
    @Column(name="fieldName")
    @Display(name="字段名")
    public String fieldName;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
