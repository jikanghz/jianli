package com.jianli.sys.dao;

import com.jianli.sys.domain.SysCode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;

public interface SysCodeDao extends Mapper<SysCode>, MySqlMapper<SysCode> {

    @Select("SELECT codeValue,codeName FROM sys_code WHERE codeCategory = #{codeCategory} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<LinkedHashMap<String, Object>> getCodeList(@Param("codeCategory") String codeCategory);

    @Select("SELECT codeValueText AS codeValue,codeName FROM sys_code WHERE codeCategory = #{codeCategory} AND deleted = 0 AND status=1 ORDER BY itemOrder")
    public List<LinkedHashMap<String, Object>> getCodeTextList(@Param("codeCategory") String codeCategory);

    @Select("SELECT codeName from sys_code where codeValue=#{codeValue} AND codeCategory=#{codeCategory} AND deleted = 0 AND status=1")
    public String getCodeName(@Param("codeValue") Integer codeValue,@Param("codeCategory") String codeCategory);
}
