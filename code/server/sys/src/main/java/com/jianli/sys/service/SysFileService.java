package com.jianli.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.service.BaseService;
import com.jianli.sys.dao.SysFileDao;
import com.jianli.sys.domain.SysFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("sysFile")
public class SysFileService extends BaseService {

    @Autowired
    private SysFileDao sysFileDao;

    public SysFile insert(String fileName, String filePath, String fileUrl, String fileSuffix, Long userId, Long tenantId)
    {
        SysFile sysFile = new SysFile();

        sysFile.fileName = fileName;
        sysFile.filePath = filePath;
        sysFile.fileUrl = fileUrl;
        sysFile.fileSuffix = fileSuffix;
        sysFile.tenantId = tenantId;
        sysFile.setDefault(userId);

        sysFileDao.insert(sysFile);

        return  sysFile;
    }

    public void setEntity(JSONArray fileList, String entity, String entityId, String fieldName, Long userId) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            JSONObject file = fileList.getJSONObject(i);
            idList.add(file.getLong("fileId"));
        }
        setEntity(idList, entity, entityId, fieldName, userId);
    }

    public void setEntity(List<Long> ids, String entity, String entityId, String fieldName, Long userId)
    {
        if(ids == null )
        {
            return;
        }

        List<SysFile> dbSysFiles = sysFileDao.list(entity, entityId, fieldName);

        List<Long> dbIds = dbSysFiles.stream().map(SysFile::getId).collect(Collectors.toList());

        List<Long> newIds = ids.stream().filter(id ->!dbIds.contains(id)).collect(Collectors.toList());
        for (Long newId : newIds)
        {
            SysFile sysFile = sysFileDao.selectByPrimaryKey(newId);
            sysFile.entity = entity;
            sysFile.entityId = entityId;
            sysFile.fieldName = fieldName;
            sysFile.setDefault(userId);
            sysFileDao.updateByPrimaryKey(sysFile);
        }

        List<SysFile> deleteItems = dbSysFiles.stream().filter(dbSysFile ->!ids.contains(dbSysFile.id)).collect(Collectors.toList());

        for(SysFile item : deleteItems)
        {
            item.setDefault(userId);
            item.deleted = true;
            sysFileDao.updateByPrimaryKey(item);
        }
    }

    public JSONArray list( String entity,  Object entityId, String fieldName) throws Exception {
        if(entityId == null)
        {
            entityId = "";
        }
        return Ext.toJArray(sysFileDao.listMap(entity, entityId.toString(), fieldName));
    }

}
