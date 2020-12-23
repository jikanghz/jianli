package com.jianli.web.controller;

import com.jianli.common.Ext;
import com.jianli.common.controller.BaseApiController;
import com.jianli.common.controller.ControllerUtil;
import com.jianli.common.excel.Excel;
import com.jianli.common.exception.BadRequestException;
import com.jianli.common.exception.UnauthorizedException;
import com.jianli.common.oss.Oss;
import com.jianli.common.service.JsonRequest;
import com.jianli.common.service.JsonResponse;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.dao.SysUserDao;
import com.jianli.sys.domain.SysFile;
import com.jianli.sys.domain.SysLog;
import com.jianli.sys.service.SysFileService;
import com.jianli.sys.service.SysLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController extends BaseApiController  {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private Oss oss;

    @Autowired
    private SysUserDao sysUserDao;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private ControllerUtil controllerUtil;

    @Resource
    private SecurityUtil securityUtil;

    @Autowired
    private SysLogService sysLogService;

    @Override
    protected BeanFactory getServiceFactory() {
        return beanFactory;
    }

    @PostMapping("/post/{service}/{method}")
    public void post(HttpServletRequest request, HttpServletResponse response, @PathVariable String service, @PathVariable String method) {
        JsonResponse jsonResponse = null;
        PrintWriter out = null;
        try {
            response.setContentType("application/json; charset=utf-8");
            out = response.getWriter();

            JsonRequest jsonRequest = createJsonRequest(request);
            jsonRequest.setService(service);
            jsonRequest.setMethod(method);

            jsonResponse = processRequest(request, jsonRequest);
        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);
        }

        if (out != null && jsonResponse != null) {
            out.println(jsonResponse);
        }
    }

    @PostMapping("/front/{service}/{method}")
    public void front(HttpServletRequest request, HttpServletResponse response, @PathVariable String service, @PathVariable String method) {
        JsonResponse jsonResponse = null;
        PrintWriter out = null;
        try {
            response.setContentType("application/json; charset=utf-8");
            out = response.getWriter();

            JsonRequest jsonRequest = createJsonRequest(request);
            jsonRequest.setService(service);
            jsonRequest.setMethod(method);

            jsonResponse = processRequest(request, jsonRequest);
        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);
        }

        if (out != null && jsonResponse != null) {
            out.println(jsonResponse);
        }
    }

    @RequestMapping("/export/{service}/{method}")
    public void export(HttpServletRequest request, HttpServletResponse response, @PathVariable String service, @PathVariable String method) {
        JsonResponse jsonResponse = null;
        try {
            response.setContentType("application/json; charset=utf-8");

            if (!request.getMethod().toLowerCase().equals("get")) {
                throw new BadRequestException("请用Get方式调用");
            }
            String en = request.getParameter("jsonRequest");
            if (en == null) {
                throw new BadRequestException("请求参数错误");
            }

            try {

                String de = Ext.base64Decode(en);
                String data = de; //URLDecoder.decode(de, "UTF-8");

                JsonRequest jsonRequest = JsonRequest.parse(data);
                if (!Ext.isNullOrEmpty(jsonRequest.getToken())) {
                    jsonRequest.setUserId(securityUtil.getUserId(jsonRequest.getToken()));
                }
                jsonRequest.setService(service);
                jsonRequest.setMethod(method);

                jsonResponse = processRequest(request, jsonRequest);
            } catch (Exception e) {
                jsonResponse = createExceptionResposne(e);
                logger.error("\nurl:\n" + request.getRequestURL() + "\nResponseData" + jsonResponse.toString(), e);
            }

            String fileName = jsonResponse.getData().getString("FileName");
            if (fileName == null) {
                fileName = Ext.toDateString(new Date(), "yyyyMMddHHmmss") + ".xls";
            }

            response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("gb2312"), "ISO8859-1"));

            OutputStream out = response.getOutputStream();
            if (jsonResponse.isOK()) {
                Excel.write(jsonResponse.getData(), out);
            } else {
                Excel.write(jsonResponse.getMessage(), out);
            }
            out.flush();
            out.close();
        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);
            logger.error("\nurl:\n" + request.getRequestURL() + "\nResponseData" + jsonResponse.toString(), ex);
        }
    }

    @RequestMapping(value = "/upload", consumes = "multipart/form-data")
    public JsonResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        JsonResponse jsonResponse = new JsonResponse();
        try {
            String token = controllerUtil.getToken(request);
            Long userId = securityUtil.getUserId(token);
            if (Ext.isNullOrZero(userId)) {
                throw new UnauthorizedException("请重新登录后再试");
            }

            String fileName = file.getOriginalFilename();
            InputStream stream = file.getInputStream();
            String subPath = request.getParameter("subPath");
            LinkedHashMap<String, Object> result = oss.upload(fileName, subPath, stream);
            Long tenantId = securityUtil.getTenantId(token);
            SysFile sysFile = sysFileService.insert(result.get("fileName").toString(),result.get("filePath").toString(),result.get("fileUrl").toString(),result.get("fileSuffix").toString(), userId, tenantId);
            jsonResponse.getData().put("fileId", sysFile.id);
            jsonResponse.getData().put("fileUrl", sysFile.fileUrl);
            jsonResponse.getData().put("fileName", fileName);
            jsonResponse.getData().put("uploadTime", Ext.toDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
            jsonResponse.getData().put("userId", userId);
            jsonResponse.getData().put("userName", sysUserDao.getUserName(userId));

        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);
        }
        return jsonResponse;
    }

    @RequestMapping(value = "/import", consumes = "multipart/form-data")
    public JsonResponse importFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        JsonResponse jsonResponse = new JsonResponse();
        try {
            String token = controllerUtil.getToken(request);
            Long userId = securityUtil.getUserId(token);
            if (Ext.isNullOrZero(userId)) {
                throw new UnauthorizedException("请重新登录后再试");
            }

            String fileName = file.getOriginalFilename();
            InputStream stream = file.getInputStream();
            LinkedHashMap<String, Object> result = oss.upload(fileName, "import/",stream);
            List<List<String>> fileInfo = Excel.read(result.get("filePath").toString());
            jsonResponse.getData().put("file",fileInfo);

        } catch (Exception ex) {
            jsonResponse = createExceptionResposne(ex);
        }
        return jsonResponse;
    }

    @Override
    protected void log(JsonRequest jsonRequest, JsonResponse jsonResponse, Date start, Date end)
    {
        if(controllerUtil.needLog(jsonRequest.getService()+"/" + jsonRequest.getMethod()))
        {
            SysLog sysLog = new SysLog();
            sysLog.service = jsonRequest.getService();
            sysLog.method = jsonRequest.getMethod();
            sysLog.request = jsonRequest.toString();
            sysLog.response = jsonResponse.toString();
            sysLog.ip = jsonRequest.getIP();
            sysLog.duration = end.getTime() - start.getTime();
            sysLog.createBy = jsonRequest.getUserId();
            sysLog.createTime = new Date();

            sysLogService.insert(sysLog);
        }
    }
}


