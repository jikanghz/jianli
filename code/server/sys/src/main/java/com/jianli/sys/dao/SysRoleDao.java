package com.jianli.sys.dao;

import com.jianli.sys.domain.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface SysRoleDao extends Mapper<SysRole>, MySqlMapper<SysRole> {

    @Select("SELECT * FROM sys_role WHERE id IN (SELECT roleId FROM sys_user_role WHERE userId = #{userId} AND deleted = 0 AND status=1 ) AND deleted = 0 AND status=1")
    public List<SysRole> listUserRole(@Param("userId") Long userId);

    @Select("SELECT id AS codeValue, roleName AS codeName FROM sys_role WHERE (tenantId = 0 OR tenantId = #{tenantId}) AND deleted = 0 ORDER BY roleName")
    public List<LinkedHashMap<String, Object>> listCodeTable(@Param("tenantId") Long tenantId);
}
