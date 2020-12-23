package com.jianli.business.dao;

import com.jianli.business.domain.Project;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface ProjectDao extends Mapper<Project>, MySqlMapper<Project> {
}
