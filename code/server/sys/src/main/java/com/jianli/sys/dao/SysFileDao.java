package com.jianli.sys.dao;

import com.jianli.sys.domain.SysFile;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;

public interface SysFileDao extends Mapper<SysFile>, MySqlMapper<SysFile> {

    @Select("SELECT * FROM sys_file WHERE entity = #{entity} AND entityId = #{entityId} AND fieldName = #{fieldName} AND deleted = 0 ORDER BY id")
    public List<SysFile> list(@Param("entity") String entity, @Param("entityId") String entityId, @Param("fieldName") String fieldName);

    @Select("SELECT id AS fileId,fileName,fileUrl FROM sys_file WHERE entity = #{entity} AND entityId = #{entityId} AND fieldName = #{fieldName} AND deleted = 0 ORDER BY id")
    public List<LinkedHashMap<String, Object>> listMap(@Param("entity") String entity, @Param("entityId") String entityId, @Param("fieldName") String fieldName);

}
