package com.jianli.sys.dao;

import com.jianli.sys.domain.SysUserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.List;


public interface SysUserRoleDao extends Mapper<SysUserRole>, MySqlMapper<SysUserRole> {

    @Select("SELECT * FROM sys_user_role WHERE userId = #{userId} AND deleted = 0 AND status = 1")
    public List<SysUserRole> listUserRole(@Param("userId") Long userId);

}
