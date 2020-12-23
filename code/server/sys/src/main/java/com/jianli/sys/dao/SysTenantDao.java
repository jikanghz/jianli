package com.jianli.sys.dao;

import com.jianli.sys.domain.SysTenant;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface SysTenantDao extends Mapper<SysTenant>, MySqlMapper<SysTenant> {
}
