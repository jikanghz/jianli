package com.jianli.sys.service;

import com.jianli.common.dao.LookupDao;
import com.jianli.common.exception.InternalKnownException;
import com.jianli.common.service.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("lookup")
public class LookupService extends BaseService {

    @Autowired
    private BeanFactory beanFactory;


    public JsonResponse search(JsonRequest jsonRequest) throws Exception {
        JsonResponse response = JsonResponse.create(jsonRequest);

        String lookupName = jsonRequest.getData().getString("lookupName");
        LookupDao lookupDao = (LookupDao) beanFactory.getBean(lookupName);
        if (lookupDao == null) {
            throw new InternalKnownException("lookup对象" + lookupName + "创建失败");
        }

        response.getData().put("entityList", lookupDao.search(jsonRequest));

        return response;
    }


}
