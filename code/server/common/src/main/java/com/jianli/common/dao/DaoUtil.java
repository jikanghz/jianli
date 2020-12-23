package com.jianli.common.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.exception.InternalKnownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DaoUtil {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JSONObject page(String select, List<QueryCondition> conditions, PageInfo pageInfo) {
        return page(select, conditions, pageInfo.orderBy, pageInfo.pageNumber, pageInfo.pageSize, null);
    }

    public JSONObject page(String select, List<QueryCondition> conditions, PageInfo pageInfo, String countSelect) {
        return page(select, conditions, pageInfo.orderBy, pageInfo.pageNumber, pageInfo.pageSize, countSelect);
    }

    public JSONObject page(String select, List<QueryCondition> conditions, String orderBy, int pageNumber, int pageSize, String countSelect) {
        JSONObject data = new JSONObject(true);

        StringBuilder where = new StringBuilder();

        if(countSelect == null)
        {
            countSelect = select;
        }

        if (conditions != null) {
            if(conditions.size() > 0)
            {
                where.append(" WHERE ");
            }

            for (int i = 0; i < conditions.size(); ++i) {
                QueryCondition condition = conditions.get(i);
                if (i > 0) {
                    where.append(" AND ");
                }
                if(!condition.fieldName.contains(",")||condition.fieldName.contains("(")) {
                    if (condition.isParam) {
                        where.append(condition.fieldName + " " + condition.operator + " ?");
                    } else {
                        where.append(condition.fieldName + " " + condition.operator + condition.condition);
                    }
                }
                else
                {
                    StringBuilder multiCondition = new StringBuilder();
                    String[] fieldNames = condition.fieldName.split(",");
                    for(String fieldName : fieldNames)
                    {
                        if(!Ext.isNullOrEmpty(multiCondition))
                        {
                            multiCondition.append(" OR ");
                        }
                        multiCondition.append(fieldName + " " + condition.operator + "'" + condition.condition + "'");
                    }
                    where.append("(" + multiCondition.toString() +")");
                }
            }
        }

        String sql = "";
        ArrayList<Object> paramList = new ArrayList<Object>();

        sql = select + where.toString() + " ORDER BY " + orderBy + " limit ?,?";

        for (int i = 0; i < conditions.size(); ++i) {
            QueryCondition condition = conditions.get(i);
            if (condition.isParam) {
                paramList.add(condition.condition);
            }
        }
        int start = (pageNumber - 1) * pageSize;
        paramList.add(start);
        paramList.add(pageSize);



        Object[] params = paramList.toArray();
        //logger.debug("\nDaoUtil page:\n" + sql + "\nsql:\n" + sql+ "\nparams:\n"  + paramsToString(params));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        JSONArray entityList = new JSONArray();
        data.put("entityList", entityList);

        for (Map<String, Object> row : rows) {
            JSONObject entity = new JSONObject(true);
            for (Map.Entry<String, Object> col : row.entrySet()) {
                Object value = col.getValue();
                if (value != null) {
                    entity.put(col.getKey(), col.getValue());
                } else {
                    entity.put(col.getKey(), "");
                }
            }
            entityList.add(entity);
        }

        String countSql = "SELECT COUNT(1) " + countSelect.substring(countSelect.indexOf("FROM"));
        sql = countSql + where.toString();

        paramList = new ArrayList<Object>();
        if (conditions != null) {
            for (int i = 0; i < conditions.size(); ++i) {
                QueryCondition condition = conditions.get(i);
                if (condition.isParam) {
                    paramList.add(condition.condition);
                }
            }
        }

        int totalCount = 0;
        int totalPage = 0;

        totalCount = jdbcTemplate.queryForObject(sql, paramList.toArray(), Integer.class);

        totalPage = totalCount / pageSize;
        if (totalPage % pageSize > 0) {
            totalPage += 1;
        }

        if (totalCount <= pageSize) {
            totalPage = 1;
        }

        if (totalCount == 0) {
            totalPage = 0;
        }

        JSONObject page = new JSONObject(true);
        page.put("totalCount", totalCount);
        page.put("totalPage", totalPage);
        data.put("page", page);

        return data;
    }

    public PageInfo getPageInfo(JSONObject data, String defaultOrderBy) {
        PageInfo pageInfo = new PageInfo();

        JSONObject page = data.getJSONObject("page");
        if(page != null) {
            pageInfo.pageNumber = page.getInteger("pageNumber");
            pageInfo.pageSize = page.getInteger("pageSize");
            pageInfo.orderBy = page.getString("orderBy");
            if (pageInfo.orderBy == null || pageInfo.orderBy.length() < 1) {
                pageInfo.orderBy = defaultOrderBy;
            }
        }
        else
        {
            pageInfo.pageNumber = 1;
            pageInfo.pageSize = 10;
            pageInfo.orderBy = defaultOrderBy;
        }
        return pageInfo;
    }

    public List<QueryCondition> getConditions(JSONObject data) {
        List<QueryCondition> conditions = new ArrayList<QueryCondition>();
        JSONArray dataConditions = data.getJSONArray("conditions");
        if(dataConditions != null) {
            for (int i = 0; i < dataConditions.size(); ++i) {
                JSONObject dataCondition = dataConditions.getJSONObject(i);
                boolean isParam = true;
                QueryCondition condition = new QueryCondition(dataCondition.getString("fieldName"), dataCondition.getString("operator"),
                        dataCondition.getString("condition"), Ext.toBoolean(dataCondition.get("isParam"), true));
                conditions.add(condition);
            }
        }
        return conditions;
    }


    public List<LinkedHashMap<String, Object>> list(String sql, Object[] params) {
        List<LinkedHashMap<String, Object>> list = new ArrayList<LinkedHashMap<String, Object>>();
        List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql, params);
        for(Map<String,Object> row : rows)
        {
            LinkedHashMap<String, Object> entity = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> col : row.entrySet()) {
                entity.put(col.getKey(), col.getValue());
            }
            list.add(entity);
        }
        return list;
    }

    public boolean exists(String tableName, String fieldName, String fieldValue, String keyField, Object keyValue, Long tenantId)
    {
        String sql = null;
        if(Ext.isNullOrZero(tenantId))
        {
            sql = "SELECT " + keyField + " FROM " + tableName + " WHERE " + fieldName + " = ? AND " + keyField + " <> ?";
        }
        else
        {
            sql = "SELECT " + keyField + " FROM " + tableName + " WHERE " + fieldName + " = ? AND " + keyField + " <> ?" + " AND tenantId=" + tenantId;
        }
        ArrayList<Object> params = new ArrayList<Object> ();
        params.add(fieldValue);
        params.add(keyValue);
        List<LinkedHashMap<String, Object>> rows = list(sql, params.toArray());
        if(rows.size() > 0)
        {
            return true;
        }

        return false;
    }

    public void checkExists(String tableName, String fieldName, String fieldValue, String fieldDisplayName, String keyField, Object keyValue, Long tenantId) {
        if (Ext.isNullOrEmpty(fieldValue)) {
            return;
        }
        if (exists(tableName, fieldName, fieldValue, keyField, keyValue, tenantId)) {
            throw new InternalKnownException(fieldDisplayName + fieldValue + "已存在");
        }
    }
}
