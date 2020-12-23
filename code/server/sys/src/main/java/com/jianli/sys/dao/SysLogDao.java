package com.jianli.sys.dao;

import com.jianli.sys.domain.SysLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;


public interface SysLogDao extends Mapper<SysLog>, MySqlMapper<SysLog> {
}
