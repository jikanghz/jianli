package com.jianli.sys.dao.lookup;

import com.jianli.common.dao.LookupDao;
import org.springframework.stereotype.Component;

@Component("regionLookup")
public class RegionLookup extends LookupDao {
    public RegionLookup()
    {
        tableName = "sys_region";
        codeValueField = "regionCode";
        codeNameField = "regionName";
        defaultOrder = "regionName";
    }
}


