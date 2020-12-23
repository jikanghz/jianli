package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.service.BaseService;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.sys.dao.SysModuleDao;
import com.jianli.sys.domain.SysModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service("sysModule")
public class SysModuleService extends BaseService {

    @Autowired
    private SysModuleDao sysModuleDao;


    @Autowired
    SysCodeService sysCodeService;

    public JsonResponse list(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        List<LinkedHashMap<String, Object>> modules = sysModuleDao.listAll(4);

        JSONArray moduleTreeList = new JSONArray();
        JSONArray moduleList = Ext.toJArray(modules);
        toTree(moduleTreeList, moduleList, "0", true);

        response.getData().put("entityList", moduleTreeList);

        response.getData().put("moduleType", sysCodeService.getCodeList("moduleType"));

        modules = sysModuleDao.listAllSimple(3);
        moduleTreeList = new JSONArray();
        moduleList = Ext.toJArray(modules);
        toTree(moduleTreeList, moduleList, "0", false);
        response.getData().put("entityListSimple", moduleTreeList);

        return response;
    }


    public JsonResponse get(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long id = jsonRequest.getData().getLong("id");
        SysModule entity = null;
        if(Ext.isNullOrZero(id))
        {
            entity = new SysModule();
        }
        else
        {
            entity = get(id);
        }

        JSONObject entityData = entity.toJObject();
        response.getData().put("entity", entityData);
        return response;
    }

    private SysModule get(Long id) throws Exception
    {
        SysModule entity = sysModuleDao.selectByPrimaryKey(id);
        if(entity == null || entity.deleted)
        {
            throw new BadRequestException("模块不存在");
        }
        return entity;
    }

    @Transactional
    public JsonResponse insert(JsonRequest jsonRequest) throws Exception {
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        SysModule entity = new SysModule();

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysModuleDao.insert(entity);

        return response;
    }


    @Transactional
    public  JsonResponse update(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        validate(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysModule entity = get(id);

        entity.set(jsonRequest.getData());
        entity.setDefault(jsonRequest.getUserId());

        sysModuleDao.updateByPrimaryKey(entity);

        return response;
    }


    private void validate(JsonRequest jsonRequest)
    {
        JSONArray parentIds = jsonRequest.getData().getJSONArray("parentId");
        if(parentIds == null || parentIds.size() < 1)
        {
            jsonRequest.getData().put("parentId", 0);
        }
        else
        {
            jsonRequest.getData().put("parentId", parentIds.getLong(parentIds.size()-1));
        }
    }


    public  JsonResponse delete(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        SysModule entity = get(id);

        entity.deleted = true;
        entity.setDefault(jsonRequest.getUserId());

        sysModuleDao.updateByPrimaryKey(entity);

        return response;
    }


    public void toTree(JSONArray treeList, JSONArray list, String parentId, boolean allowEmpty) {
        for (int i = 0; i < list.size(); ++i) {
            JSONObject module = list.getJSONObject(i);
            if (module.getString("parentId") != null && module.getString("parentId").equals(parentId)) {
                JSONArray childModules = new JSONArray();
                toTree(childModules, list, module.getString("id"), allowEmpty);
                if (allowEmpty || childModules.size() > 0) {
                    module.put("moduleList", childModules);
                }
                treeList.add(module);
            }
        }

        treeList.sort((a, b) -> {
            return ((JSONObject) a).getInteger("itemOrder").compareTo(((JSONObject) b).getInteger("itemOrder"));
        });
    }


    public  JsonResponse listStandardFunction(JsonRequest jsonRequest) throws Exception{
        JsonResponse response = JsonResponse.create(jsonRequest);
        Long id = jsonRequest.getData().getLong("id");
        JSONArray standardFunctions = new JSONArray();
        List<SysModule> children = sysModuleDao.listChildren(id);

        String service = "";

        if(children != null)
        {
            for (SysModule child : children)
            {
                if(child.moduleName.equals("列表") ||
                        child.moduleName.equals("查看") ||
                        child.moduleName.equals("新建") ||
                        child.moduleName.equals("修改") ||
                        child.moduleName.equals("删除") ||
                        child.moduleName.equals("导出"))
                {
                    standardFunctions.add(0, child.moduleName);
                    if(Ext.isNullOrEmpty(service))
                    {
                        service = child.service.toString();
                    }
                }
            }
        }

        response.getData().put("standardFunctions", standardFunctions);
        response.getData().put("service", service);
        return response;
    }

    public  JsonResponse setStandardFunction(JsonRequest jsonRequest) throws Exception{
        authentication(jsonRequest);
        authorization(jsonRequest);

        JsonResponse response = JsonResponse.create(jsonRequest);

        Long parentId = jsonRequest.getData().getLong("parentId");
        Ext.checkRequired(parentId, "上级模块id");
        String service = jsonRequest.getData().getString("service");
        Ext.checkRequired(service, "对象名称");

        JSONArray standardFunctions = jsonRequest.getData().getJSONArray("standardFunctions");
        if(standardFunctions != null)
        {
            List<SysModule> children = sysModuleDao.listChildren(parentId);
            Object[] names =  standardFunctions.toArray();
            int i = children.size();
            for(Object name :  names)
            {
                Optional<SysModule> optional = children.stream().filter(item -> item.moduleName.equals(name.toString())).findFirst();
                if (!optional.isPresent()) {
                    ++i;

                    SysModule entity = new SysModule();
                    entity.moduleName = name.toString();
                    entity.moduleType = 4;
                    entity.parentId = parentId;
                    entity.service = service;
                    if(name.equals("列表"))
                    {
                        entity.method = "list";
                    }
                    else if(name.equals("查看"))
                    {
                        entity.method = "get";
                    }
                    else if(name.equals("查看"))
                    {
                        entity.method = "get";
                    }
                    else if(name.equals("新建"))
                    {
                        entity.method = "insert";
                    }
                    else if(name.equals("修改"))
                    {
                        entity.method = "update";
                    }
                    else if(name.equals("删除"))
                    {
                        entity.method = "delete";
                    }
                    else if(name.equals("导出"))
                    {
                        entity.method = "export";
                    }
                    entity.itemOrder = i * 10;
                    entity.setDefault(jsonRequest.getUserId());
                    sysModuleDao.insert(entity);
                }
            }
        }
        return response;
    }
}
