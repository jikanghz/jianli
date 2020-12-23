package com.jianli.common.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.service.JsonRequest;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class LookupDao {
    private static CharSql charSql = new CharSql();

    protected  String lookupName;

    protected String tableName;

    protected String  codeValueField;
    protected String  codeNameField;

    protected String  codeInfoField;

    public int listNumber;

    protected String defaultOrder;

    protected String getFirstCodeNameField() {
        return codeNameField.split(",")[0];
    }

    @Autowired
    private DaoUtil daoUtil;

    public LookupDao()
    {
        lookupName = "";
        tableName =  "";
        codeValueField =  "";
        codeNameField =  "";
        codeInfoField =  "";
        defaultOrder =  "";
        listNumber = 50;
    }

    protected String getLookupFields()
    {
        String fields = codeValueField + " AS codeValue, " + getFirstCodeNameField() + " AS codeName";
        if (!Ext.isNullOrEmpty(codeInfoField))
        {
            fields += ", " + codeInfoField + "AS codeInfo";
        }
        return fields;
    }

    public List<LinkedHashMap<String, Object>> search(JsonRequest jsonRequest) throws  Exception {
        String inputValue = "";
        String codeValue = "";

        inputValue = jsonRequest.getData().getString("q");
        codeValue = jsonRequest.getData().getString("codeValue");

        if (Ext.isNullOrEmpty(inputValue)) {
            inputValue = "%";
        }

        String condition = getCondition(inputValue, jsonRequest);
        if (!Ext.isNullOrEmpty(condition)) {
            condition = " WHERE " + condition;
            if (!Ext.isNullOrEmpty(codeValue)) {
                condition = condition + " AND " + codeValueField + " = '" + codeValue + "'";
            }
        }

        String order = "";
        if (!Ext.isNullOrEmpty(defaultOrder)) {
            order = " ORDER BY " + defaultOrder;
        }

        String sql = "SELECT " + getLookupFields() + " FROM " + tableName + " " + condition + order + " limit ?";

        ArrayList<Object> params = new ArrayList<Object> ();
        params.add(listNumber);

        List<LinkedHashMap<String, Object>> rows = daoUtil.list(sql, params.toArray());

        return rows;
    }

    protected String getCondition(String inputValue, JsonRequest jsonRequest)
    {
        String condition = "";
        inputValue = inputValue.replace("'", "''");

        String[] fields = codeNameField.split(",");

        for (String field : fields)
        {
            if (!Ext.isNullOrEmpty(condition))
            {
                condition += " OR ";
            }

            if (Ext.isChinese(inputValue))
            {
                condition += "(" + field + " LIKE " + "'%" + inputValue + "%')";
            }
            else
            {
                condition += "((" + field + " LIKE '" + inputValue.toLowerCase() + "%') OR (" + getCharFullCondition(field, inputValue) + "))";
            }
        }
        return condition;
    }

    public static String getCharFullCondition(String fieldName, String fieldValue) {
        StringBuilder sql = new StringBuilder(1024);
        int i = 1;
        char[] chars = fieldValue.toCharArray();
        for (char c : chars) {
            if (i > 1) {
                sql.append(" AND ");
            }
            String startWord, endWord;
            char upper =   Ext.toUpper(c);
            startWord = charSql.start(upper);
            endWord = charSql.end(upper);
            if(Ext.isNullOrEmpty(startWord) || Ext.isNullOrEmpty(endWord))
            {
                startWord = String.valueOf(c);
                endWord = String.valueOf(c);
            }
            String subStr = "CONV(HEX(SUBSTRING(CONVERT(" + fieldName + " USING gbk ) , " + i + ",1 ) ) , 16, 10)";
            sql.append("((" + subStr + " >= '" + startWord + "' AND " + subStr + " <='" + endWord + "') OR " + subStr + " = '" + String.valueOf(c) + "')");
            ++i;
        }

        return sql.toString();
    }

    public JSONArray  fillCodeTable(JSONObject data, JSONObject entity, String fieldName, Long tenantId)
    {
        String sql = null;
        String fieldValue = entity.getString(fieldName);
        String field = codeValueField + " AS codeValue, " + getFirstCodeNameField() + " AS codeName";
        if(!Ext.isNullOrEmpty(fieldValue))
        {
            String where =  codeValueField + " = '" + fieldValue + "'";
            if(Ext.isNullOrZero(tenantId)) {
                sql = "SELECT " + field + " FROM " + tableName + " WHERE " + where + " OR 1=1 ORDER BY CONVERT(" + getFirstCodeNameField() + " USING gbk) limit ?";
            }
            else
            {
                sql = "SELECT " + field + " FROM " + tableName + " WHERE " + where + " OR tenantId=" + tenantId + " ORDER BY CONVERT(" + getFirstCodeNameField() + " USING gbk) limit ?";
            }
        }
        else
        {
            if(Ext.isNullOrZero(tenantId)) {
                sql = "SELECT " + field + " FROM " + tableName + " ORDER BY CONVERT(" + getFirstCodeNameField() + " USING gbk) limit ?";
            }
            else
            {
                sql = "SELECT " + field + " FROM " + tableName + " WHERE tenantId=" + tenantId + " ORDER BY CONVERT(" + getFirstCodeNameField() + " USING gbk) limit ?";
            }
        }

        ArrayList<Object> params = new ArrayList<Object> ();
        params.add(listNumber);
        List<LinkedHashMap<String, Object>> rows = daoUtil.list(sql, params.toArray());
        data.put(fieldName, rows);

        JSONArray codeTable = null;
        if(data.get(fieldName) != null)
        {
            codeTable = data.getJSONArray(fieldName);
        }
        return codeTable;
    }

    public JSONArray  fillCodeTables(JSONObject data, JSONArray entityList, String fieldName) {
        JSONArray codeTable = null;
        if(entityList != null && entityList.size() > 0)
        {
            StringBuilder stringBuilder = new StringBuilder();
            String codeValues = "";
            List<String> codeValueList = new ArrayList<String>();
            for (int i=0; i<entityList.size(); ++i)
            {
                JSONObject entity = entityList.getJSONObject(i);

                String codeValue = entity.getString(fieldName);


                if (!Ext.isNullOrEmpty(codeValue) && !codeValueList.contains(codeValue))
                {
                    codeValueList.add(codeValue);
                }
            }
            codeValues = StringUtils.join(codeValueList, ',');

            String sql = null;
            if (Ext.isNullOrEmpty(codeValues))
            {
                sql = "SELECT " + codeValueField + " AS codeValue, " + getFirstCodeNameField() + " AS codeName FROM " + tableName + " WHERE 1=0";
            }
            else
            {
                sql = "SELECT " + codeValueField + " AS codeValue, " + getFirstCodeNameField() + " AS codeName FROM " + tableName + " WHERE " + codeValueField + " IN (" + codeValues + ")";
            }

            List<LinkedHashMap<String, Object>> rows = daoUtil.list(sql, null);
            data.put(fieldName, rows.toArray());
            codeTable = data.getJSONArray(fieldName);
        }
        return codeTable;
    }

}
