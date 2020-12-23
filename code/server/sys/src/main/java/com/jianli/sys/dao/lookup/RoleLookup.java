package com.jianli.sys.dao.lookup;

import com.jianli.common.dao.LookupDao;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("roleLookup")
public class RoleLookup extends LookupDao {
    @Autowired
    private SecurityUtil securityUtil;

    public RoleLookup()
    {
        tableName = "sys_role";
        codeValueField = "id";
        codeNameField = "roleName";
        defaultOrder = "roleName";
    }

    @Override
    protected String getCondition(String inputValue, JsonRequest jsonRequest) {
        String condition = super.getCondition(inputValue, jsonRequest);

        condition += " AND (tenantId = " + securityUtil.getTenantId(jsonRequest.getToken()) + " OR tenantId = 0)";
        return  condition;
    }
}


