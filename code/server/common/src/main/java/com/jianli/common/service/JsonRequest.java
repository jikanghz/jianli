package com.jianli.common.service;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class JsonRequest {


    private JSONObject root = null;

    private String service = "";
    private String method = "";

    private String ip = "";
    private Long userId;
    public JSONObject root()
    {
        return root;
    }


    public JsonRequest()
    {
        root = new JSONObject(true);
    }

    public JsonRequest(String json)
    {
        root = JSONObject.parseObject(json, Feature.OrderedField);
        setUserId(new Long(0));
    }

    public String getService()
    {
        return service;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }



    public String getIP()
    {
        return ip;
    }

    public void setIP(String ip){ this.ip = ip; }

    public String getToken()
    {
        return root().getString("token");
    }

    public void setToken(String token)
    {
        root().put("token", token);
    }

    public Long getUserId() {
        return  userId;
    }

    public void setUserId(Long userId){
        this.userId = userId;
    }

    public String getClient()
    {
        return root().getString("client");
    }

    public void setClient(String client)
    {
        root().put("client", client);
    }


    public JSONObject getData()
    {
        JSONObject data = root().getJSONObject("data");
        if(data == null)
        {
            data = new JSONObject(true);
            setData(data);
        }
        return data;
    }

    public void setData(JSONObject data)
    {
        root().put("data", data);
    }

    public static JsonRequest parse(String json)
    {
        JsonRequest jsonRequest = new JsonRequest(json);
        return jsonRequest;
    }


    public String toString(){
        return root().toString();
    }

}
