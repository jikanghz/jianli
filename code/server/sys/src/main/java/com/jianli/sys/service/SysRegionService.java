package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.service.BaseService;
import com.jianli.sys.dao.SysRegionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service("sysRegion")
public class SysRegionService extends BaseService {

    @Autowired
    private SysRegionDao sysRegionDao;


    public List<LinkedHashMap<String, Object>> listAll(int layer)
    {
        return sysRegionDao.listAll(layer);
    }


    public void toTree(JSONArray treeList, JSONArray list, String parentId, boolean allowEmpty) {
        for (int i = 0; i < list.size(); ++i) {
            JSONObject module = list.getJSONObject(i);
            if (module.getString("parentId").equals(parentId)) {
                JSONArray childModules = new JSONArray();
                toTree(childModules, list, module.getString("id"), allowEmpty);
                if (allowEmpty || childModules.size() > 0) {
                    module.put("children", childModules);
                }

                JSONObject node = new JSONObject(true);
                node.put("regionCode", module.getString("regionCode"));
                node.put("regionName", module.getString("regionName"));
                node.put("children", module.getJSONArray("children"));
                treeList.add(node);
            }
        }
    }

    public String getParentRegionCode(String regionCode)
    {
        return  sysRegionDao.getParentRegionCode(regionCode);
    }


    public String getParentRegionName(String regionCode){
        return sysRegionDao.getParentRegionName(regionCode);
    }


    public String getRegionName(String regionCode){
        return sysRegionDao.getRegionName(regionCode);
    }


    public JSONArray getParentRegionCodes(String regionCode)
    {
        JSONArray parentRegionCodes = new JSONArray();

        if(!Ext.isNullOrEmpty(regionCode)) {
            parentRegionCodes.add(0, regionCode);
            String parentCode = getParentRegionCode(regionCode);
            if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                parentRegionCodes.add(0, parentCode);
                parentCode = getParentRegionCode(parentCode);
                if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                    parentRegionCodes.add(0, parentCode);
                    parentCode = getParentRegionCode(parentCode);
                    if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                        parentRegionCodes.add(0, parentCode);
                    }
                }
            }
        }

        return parentRegionCodes;
    }

    public JSONArray getParentRegionNames(String regionCode)
    {
        JSONArray parentRegionNames = new JSONArray();

        if(!Ext.isNullOrEmpty(regionCode)) {
            parentRegionNames.add(0, getRegionName(regionCode));
            String parentCode = getParentRegionCode(regionCode);
            String parentName = getParentRegionName(regionCode);
            if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                parentRegionNames.add(0, parentName);
                parentName = getParentRegionName(parentCode);
                parentCode = getParentRegionCode(parentCode);
                if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                    parentRegionNames.add(0, parentName);
                    parentName = getParentRegionName(parentCode);
                    parentCode = getParentRegionCode(parentCode);
                    if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                        parentRegionNames.add(0, parentName);
                    }
                }
            }
        }

        return parentRegionNames;
    }


    public String getRegionNames(String regionCode)
    {
        String parentRegionNames="";

        if(!Ext.isNullOrEmpty(regionCode)) {
            parentRegionNames = getRegionName(regionCode);
            String parentCode = getParentRegionCode(regionCode);
            String parentName = getParentRegionName(regionCode);
            if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                parentRegionNames =  parentName + parentRegionNames;
                parentName = getParentRegionName(parentCode);
                parentCode = getParentRegionCode(parentCode);
                if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                    parentRegionNames =  parentName + parentRegionNames;
                    parentName = getParentRegionName(parentCode);
                    parentCode = getParentRegionCode(parentCode);
                    if (!Ext.isNullOrEmpty(parentCode) && !parentCode.equals("0")) {
                        parentRegionNames =  parentName + parentRegionNames;
                    }
                }
            }
        }

        return parentRegionNames;
    }


}
