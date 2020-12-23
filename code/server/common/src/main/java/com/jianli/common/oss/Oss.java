package com.jianli.common.oss;

import com.aliyun.oss.OSSClient;
import com.jianli.common.Ext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;

@Component
public class Oss implements InitializingBean {

    @Value("${upload.endpoint:}")
    private String endpoint;

    @Value("${upload.accessKeyId:}")
    private String accessKeyId;

    @Value("${upload.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${upload.bucketName:}")
    private String bucketName;

    @Value("${upload.domain:}")
    public String domain;

    @Value("${upload.localPath:}")
    public String localPath;

    public void afterPropertiesSet() throws Exception {
    }


    public String getPath(String fileName) throws Exception {
        String random = Ext.getRandomString(6);
        Date date = new Date();
        String path = Ext.toDateString(date, "yyyyMMdd") + "/" + Ext.toDateString(date, "HHmmssS") + random + "." + Ext.getFileExtName(fileName);
        return path;
    }


    public LinkedHashMap<String, Object> upload(String fileName, String subPath, InputStream inputStream) throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        Date now = new Date();

        String path = "";
        if(!Ext.isNullOrEmpty(subPath))
        {
            path += subPath + "/";
        }

        File dir = new File(localPath + path);
        if (!dir.exists()) {
            dir.mkdir();
        }

        path  += Ext.toDateString(now, "yyyyMMdd");
        dir = new File(localPath + path);
        if (!dir.exists()) {
            dir.mkdir();
        }

        String newFileName = Ext.getRandomFileName(fileName);
        path += "/" + newFileName;
        FileOutputStream fileOutputStream = new FileOutputStream(localPath + path);
        byte[] b = new byte[1024];
        int length;
        while ((length = inputStream.read(b)) > 0) {
            fileOutputStream.write(b, 0, length);
        }
        fileOutputStream.close();
        String url = domain + path;

        result.put("fileName", fileName);
        result.put("filePath", path);
        result.put("fileUrl", url);
        result.put("fileSuffix", Ext.getFileExtName(fileName));

        return result;

        /*
        OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        String url = "";
        String path = getPath(fileName);
        client.putObject(bucketName, path, inputStream);
        url = domain + path;
        return url;*/
    }
}
