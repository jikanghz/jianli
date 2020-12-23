package com.jianli.sys.dao;

import com.jianli.sys.domain.SysConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;

public interface SysConfigDao extends Mapper<SysConfig>, MySqlMapper<SysConfig> {

    @Select("SELECT codeValue,codeName FROM sys_config")
    public List<LinkedHashMap<String, Object>> listAll();


    @Select("SELECT * FROM sys_config WHERE codeName = #{codeName} order by id limit 1")
    public SysConfig get(@Param("codeName") String codeName);
}
