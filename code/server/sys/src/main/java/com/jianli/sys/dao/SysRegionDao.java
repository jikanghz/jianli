package com.jianli.sys.dao;

import com.jianli.sys.domain.SysRegion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface SysRegionDao extends Mapper<SysRegion>, MySqlMapper<SysRegion> {


    @Select("SELECT * FROM sys_region WHERE layer <= #{layer} ORDER BY regionCode")
    public List<LinkedHashMap<String, Object>> listAll(@Param("layer") int layer);


    @Select("SELECT * FROM sys_region WHERE parentId = (SELECT id FROM sys_region  WHERE regionCode = #{regionCode}) ORDER BY regionCode")
    public List<LinkedHashMap<String, Object>> listChildren(@Param("regionCode") String regionCode);


    @Select("SELECT regionCode FROM sys_region WHERE id = (SELECT parentId FROM sys_region WHERE regionCode = #{regionCode})")
    public String getParentRegionCode(@Param("regionCode") String regionCode);


    @Select("SELECT regionName FROM sys_region WHERE id = (SELECT parentId FROM sys_region WHERE regionCode = #{regionCode})")
    public String getParentRegionName(@Param("regionCode") String regionCode);


    @Select("SELECT regionName FROM sys_region WHERE regionCode = #{regionCode}")
    public String getRegionName(@Param("regionCode") String regionCode);

}
