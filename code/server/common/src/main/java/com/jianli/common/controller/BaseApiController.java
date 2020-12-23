package com.jianli.common.controller;

import com.jianli.common.Ext;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.exception.BaseException;
import com.jianli.common.exception.InternalKnownException;
import com.jianli.common.service.*;
import com.jianli.common.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Date;

public abstract class BaseApiController {

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ControllerUtil controllerUtil;

    private Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean logRequest = true;
    protected boolean logResponse = true;

    protected boolean logDebug = true;


    protected BeanFactory getServiceFactory() {
        return null;
    }


    protected JsonRequest createJsonRequest(HttpServletRequest request) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        if (!request.getMethod().toLowerCase().equals("post")) {
            throw new BadRequestException("请用Post方式调用");
        }

        BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        String lineString;
        while ((lineString = streamReader.readLine()) != null) {
            stringBuilder.append(lineString);
        }
        JsonRequest jsonRequest = JsonRequest.parse(stringBuilder.toString());
        jsonRequest.setIP(controllerUtil.getIpAdrress(request));

        if (!Ext.isNullOrEmpty(jsonRequest.getToken())) {
            jsonRequest.setUserId(securityUtil.getUserId(jsonRequest.getToken()));
        }

        return jsonRequest;
    }

    protected JsonResponse processRequest(HttpServletRequest request, JsonRequest jsonRequest) {
        JsonResponse jsonResponse = null;
        Date start = new Date();
        String serviceName = jsonRequest.getService();
        String methodName = jsonRequest.getMethod();

        String requestString = "";
        if (logRequest) {
            requestString = jsonRequest.toString();
        }

        String responseString = "";
        try {
            BaseService service = (BaseService) getServiceFactory().getBean(serviceName);
            if (service == null) {
                throw new InternalKnownException("服务对象" + serviceName + "创建失败");
            }

            Class serviceClass = service.getClass();
            Method method = serviceClass.getMethod(methodName, JsonRequest.class);

            controllerUtil.protectedDemo(serviceName + "/" + methodName);

            jsonResponse = (JsonResponse) method.invoke(service, new Object[]{jsonRequest});

            if (logResponse) {
                responseString = jsonResponse.toString();
            }

            if (logDebug) {
                logger.debug("\nurl:\n" + request.getRequestURL() + "\nrequest:\n" + requestString + "\nresponse:\n" + responseString);
            }
        } catch (NoSuchMethodException ex) {
            jsonResponse = createExceptionResposne(ex);
            jsonResponse.setCode(400);
            jsonResponse.setMessage("业务方法" + methodName + "不存在");

            if (logResponse) {
                responseString = jsonResponse.toString();
            }
            logger.error("\nurl:\n" + request.getRequestURL() + "\nrequest:\n" + requestString + "\nresponse:\n" + responseString, ex);
        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);

            if (logResponse) {
                responseString = jsonResponse.toString();
            }
            logger.error("\nurl:\n" + request.getRequestURL() + "\nrequest:\n" + requestString + "\nresponse:\n" + responseString, ex);
        }

        log(jsonRequest, jsonResponse, start, new Date());

        return jsonResponse;
    }

    public JsonResponse createExceptionResposne(Exception ex) {
        ex = Ext.getInnerException(ex);
        JsonResponse jsonResponse = JsonResponse.create(null);
        String message = ex.getMessage();
        if (message == null) {
            message = ex.toString();
        }
        if (ex instanceof BaseException) {
            BaseException baseException = (BaseException) ex;
            jsonResponse.SetError(baseException.getCode(), message);
        } else {
            jsonResponse.SetError(500, message);
        }
        return jsonResponse;
    }

    protected void log(JsonRequest jsonRequest, JsonResponse jsonResponse, Date start, Date end) {
    }
}
