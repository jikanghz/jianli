package com.jianli.sys.dao;

import com.jianli.sys.domain.SysModule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface SysModuleDao extends Mapper<SysModule>, MySqlMapper<SysModule> {

    @Select("SELECT * FROM sys_module WHERE moduleType <= #{maxModuleType} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<LinkedHashMap<String, Object>> listAll(@Param("maxModuleType") int maxModuleType);

    @Select("SELECT id,moduleName,moduleType,parentId,moduleUrl,iconUrl,service,method,itemOrder FROM sys_module WHERE moduleType <= #{maxModuleType} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<LinkedHashMap<String, Object>> listAllSimple(@Param("maxModuleType") int maxModuleType);

    @Select("SELECT id,moduleName,moduleType,parentId,moduleUrl,iconUrl,service,method,itemOrder FROM sys_module WHERE id IN (SELECT moduleId FROM sys_role_module WHERE roleId IN (SELECT roleid FROM sys_user_role WHERE userid = #{userId}  AND deleted = 0 AND status=1) AND deleted = 0 AND status=1) AND moduleType <= #{maxModuleType} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<LinkedHashMap<String, Object>> listUserModule(@Param("userId") Long userId, @Param("maxModuleType") int maxModuleType);

    @Select("SELECT id,moduleName,moduleType,parentId,moduleUrl,iconUrl,service,method,itemOrder FROM sys_module WHERE id = #{id} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public LinkedHashMap<String, Object> get(@Param("id") Long id);

    @Select("SELECT * FROM sys_module WHERE parentId = #{parentId} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<SysModule> listChildren(@Param("parentId") Long parentId);
}
