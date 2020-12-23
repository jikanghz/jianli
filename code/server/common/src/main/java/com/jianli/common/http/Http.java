package com.jianli.common.http;

import com.jianli.common.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Http {

    @Autowired
    private ByteUtil byteUtil;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String post(String url, String data, String contentType, LinkedHashMap<String, Object> heads)
    {
        StringBuffer contentBuffer = new StringBuffer();
        URL postUrl = null;
        HttpURLConnection connection = null;

        try {
            postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();
            setHeads(connection, heads);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(data);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                contentBuffer.append(line);
            }
            br.close();
        } catch (Exception ex) {
            logger.error("\nurl:\n" + url + "\ndata" + data, ex);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return contentBuffer.toString();
    }

    public String post(String url, byte[] data, LinkedHashMap<String, Object> heads)
    {
        StringBuffer contentBuffer = new StringBuffer();
        URL postUrl = null;
        HttpURLConnection connection = null;
        try {
            postUrl = new URL(url);
            connection = (HttpURLConnection) postUrl.openConnection();
            setHeads(connection, heads);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "binary/octet-stream");
            OutputStream outStream = connection.getOutputStream();
            outStream.write(data);
            outStream.flush();
            outStream.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                contentBuffer.append(line);
            }
            br.close();
        } catch (Exception ex) {
            logger.error("\nurl:\n" + url, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return contentBuffer.toString();
    }

    private void setHeads(HttpURLConnection connection, LinkedHashMap<String, Object> heads)
    {
        if(heads != null) {
            for (Map.Entry<String, Object> entry : heads.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue().toString());
            }
        }
    }
}
