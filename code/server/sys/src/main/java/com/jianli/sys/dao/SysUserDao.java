package com.jianli.sys.dao;

import com.jianli.sys.domain.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;


public interface SysUserDao extends Mapper<SysUser>, MySqlMapper<SysUser> {


    @Select("SELECT * FROM sys_user WHERE loginName = #{loginName} AND deleted = 0 AND status=1")
    public  SysUser getByLoginName(@Param("loginName") String loginName);



    @Select("SELECT COUNT(*) FROM sys_user WHERE id = #{userId} AND id IN (SELECT userId FROM sys_user_role WHERE roleId = 1) AND deleted = 0 AND status=1")
    boolean isSupperAccount(@Param("userId") long userId);


    @Select("SELECT userName FROM sys_user WHERE id = #{id} AND deleted = 0 AND status=1")
    String getUserName(@Param("id") long id);

}
