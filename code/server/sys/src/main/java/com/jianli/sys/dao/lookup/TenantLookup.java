package com.jianli.sys.dao.lookup;

import com.jianli.common.dao.LookupDao;
import org.springframework.stereotype.Component;

@Component("tenantLookup")
public class TenantLookup extends LookupDao {
    public TenantLookup()
    {
        tableName = "sys_tenant";
        codeValueField = "id";
        codeNameField = "tenantName";
        defaultOrder = "tenantName";
    }
}


