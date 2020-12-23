package com.jianli.sys.dao;

import com.jianli.sys.domain.SysRoleModule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;


public interface SysRoleModuleDao extends Mapper<SysRoleModule>, MySqlMapper<SysRoleModule> {


    @Select("SELECT * FROM sys_role_module WHERE roleId = #{roleId} AND deleted = 0")
    public List<SysRoleModule> listRoleModule(@Param("roleId") Long roleId);

}
