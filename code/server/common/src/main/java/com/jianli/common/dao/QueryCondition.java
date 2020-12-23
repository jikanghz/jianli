package com.jianli.common.dao;

public class QueryCondition {
    public QueryCondition()
    {
    }

    public QueryCondition(String fieldName, String oper, String fieldValue)
    {
        this(fieldName, oper, fieldValue, true);
    }

    public QueryCondition(String fieldName,  String oper, String fieldValue, boolean isParam)
    {
        this.fieldName = fieldName;
        operator = oper;

        condition = fieldValue;
        if (operator.toLowerCase().trim() == "like" && !condition.endsWith("%"))
        {
            condition = "%" + condition + "%";
        }
        this.isParam = isParam;
    }

    public String fieldName;

    public String operator;

    public String condition;

    public boolean isParam;
}
