package com.jianli.sys.dao.lookup;

import com.jianli.common.dao.LookupDao;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("userLookup")
public class UserLookup extends LookupDao {
    @Autowired
    private SecurityUtil securityUtil;

    public UserLookup()
    {
        tableName = "sys_user";
        codeValueField = "id";
        codeNameField = "userName";
        defaultOrder = "userName";
    }

    @Override
    protected String getCondition(String inputValue, JsonRequest jsonRequest) {
        String condition = super.getCondition(inputValue, jsonRequest);

        condition += " AND tenantId = " + securityUtil.getTenantId(jsonRequest.getToken());
        return  condition;
    }
}


