package com.jianli.common.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class JsonResponse {

    private JSONObject root = null;

    public JSONObject root()
    {
        return root;
    }


    public int getCode()
    {
        return root().getInteger("code");
    }

    public void setCode(int code)
    {
        root().put("code", code);
    }

    public String getMessage()
    {
        return root().getString("message");
    }

    public void setMessage(String message)
    {
        root().put("message", message);
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


    public JsonResponse()
    {
        root = new JSONObject(true);
        root.put("code", "200");
        root.put("message", "OK");
    }

    public boolean isOK()
    {
        return getCode() == 200;
    }

    @Override
    public String toString() {
        return root().toString();
    }

    public void SetError(int code, String message)
    {
        setCode(code);
        setMessage(message);
    }


    public static JsonResponse create(JsonRequest jsonRequest)
    {
        JsonResponse jsonResponse = new JsonResponse();
        if(jsonRequest != null)
        {
        }
        return jsonResponse;
    }

    public static JsonResponse parse(String json)
    {
        JsonResponse jsonResponse = new JsonResponse();
        jsonResponse.root = JSONObject.parseObject(json, Feature.OrderedField);
        return jsonResponse;
    }

}
