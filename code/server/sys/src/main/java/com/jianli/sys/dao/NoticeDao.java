package com.jianli.sys.dao;

import com.jianli.sys.domain.Notice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

import java.util.LinkedHashMap;
import java.util.List;


public interface NoticeDao extends Mapper<Notice>, MySqlMapper<Notice> {

    @Select("SELECT id,title FROM notice WHERE tenantId = #{tenantId} AND deleted = 0 AND status=1 ORDER BY id DESC limit 0,10")
    public List<LinkedHashMap<String,Object>> getIndexNotice(@Param("tenantId") Long tenantId);
}
