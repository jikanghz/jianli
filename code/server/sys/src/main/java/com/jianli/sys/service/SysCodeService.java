package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.service.BaseService;
import com.jianli.sys.dao.SysCodeDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("sysCode")
public class SysCodeService  extends BaseService {

    @Autowired
    SysCodeDao sysCodeDao;

    public JSONArray getCodeList(String codeCategory)
    {
        List<LinkedHashMap<String, Object>> codeList = sysCodeDao.getCodeList(codeCategory);
        return Ext.toJArray(codeList);
    }


    public JSONArray getCodeTextList(String codeCategory)
    {
        List<LinkedHashMap<String, Object>> codeList = sysCodeDao.getCodeTextList(codeCategory);
        return Ext.toJArray(codeList);
    }


    public String getCodeName(JSONArray rows, String codeValue)
    {
        if (rows != null)
        {
            for(int i=0; i<rows.size(); ++i)
            {
                JSONObject row = rows.getJSONObject(i);
                if (row.getString("codeValue").equals(codeValue))
                {
                    return row.getString("codeName");
                }
            }
        }
        return "";
    }
}
