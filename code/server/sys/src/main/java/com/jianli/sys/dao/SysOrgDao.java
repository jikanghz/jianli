package com.jianli.sys.dao;

import com.jianli.sys.domain.SysOrg;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;

public interface SysOrgDao extends Mapper<SysOrg>, MySqlMapper<SysOrg> {
    @Select("SELECT * FROM sys_org WHERE tenantId = #{tenantId} AND deleted = 0 AND status=1 ORDER BY ID")
    public List<LinkedHashMap<String, Object>> listAll(@Param("tenantId") Long tenantId);

    @Select("SELECT * FROM sys_org WHERE tenantId = #{tenantId} AND deleted = 0 AND status=1 AND orgType=1 ORDER BY id DESC limit 1")
    public SysOrg getRoot(@Param("tenantId") Long tenantId);

    @Select("SELECT path FROM sys_org WHERE id = #{id}")
    public String getPath(@Param("id") Long id);
}
