package com.jianli.common.domain;


import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import com.jianli.common.domain.validate.*;

import javax.persistence.Column;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

public class BaseDomain implements Serializable {

    @Override
    public String toString() {
        try {
            JSONObject jsonObject = toJObject();
            return jsonObject.toString();
        } catch (Exception ex) {
        }
        return super.toString();
    }

    public JSONObject toJObject() throws Exception {
        JSONObject jsonObject = new JSONObject(true);
        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {

            Sensitive[] sensitives = field.getAnnotationsByType(Sensitive.class);
            if (sensitives.length > 0) {
                Sensitive sensitive = sensitives[0];
                String type = sensitive.type();
                if (type != null && (type.equals("both") || type.equals("out"))) {
                    continue;
                }
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Object value = null;
            value = field.get(this);
            if (value != null) {
                if (value instanceof Date) {
                    if (field.getName().toLowerCase().endsWith("date")) {
                        jsonObject.put(field.getName(), Ext.toDateString((Date) value, "yyyy-MM-dd"));
                    } else {
                        jsonObject.put(field.getName(), Ext.toDateString((Date) value, "yyyy-MM-dd HH:mm:ss"));
                    }
                } else {
                    jsonObject.put(field.getName(), value);
                }
            } else {
                jsonObject.put(field.getName(), "");
            }
        }
        return jsonObject;
    }

    public void set(JSONObject data) throws Exception {
        Field[] fields = this.getClass().getFields();

        for (Field field : fields) {
            Sensitive[] sensitives = field.getAnnotationsByType(Sensitive.class);
            if (sensitives.length > 0) {
                Sensitive sensitive = sensitives[0];
                String type = sensitive.type();
                if (type != null && (type.equals("both") || type.equals("in"))) {
                    continue;
                }
            }
            String key = field.getName();
            if (!data.containsKey(key)) {
                continue;
            }

            Object value = data.get(key);
            if (value != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                if (field.getType().equals(String.class)) {
                    field.set(this, value);
                } else if (field.getType() == Boolean.class) {
                    field.set(this, Ext.toBoolean(value));
                } else if (field.getType() == Integer.class) {
                    field.set(this, Ext.toInteger(value));
                } else if (field.getType() == Long.class) {
                    field.set(this, Ext.toLong(value));
                } else if (field.getType() == Float.class) {
                    field.set(this, Ext.toFloat(value));
                } else if (field.getType() == Double.class) {
                    field.set(this, Ext.toDouble(value));
                } else if (field.getType() == BigDecimal.class) {
                    field.set(this, Ext.toBigDecimal(value));
                } else if (field.getType() == Date.class) {
                    field.set(this, Ext.toDate(value));
                }

                String displayName = key;

                Display display = null;

                display = field.getAnnotationsByType(Display.class)[0];
                if (display != null) {
                    displayName = display.name();
                }

                if (field.isAnnotationPresent(Required.class)) {
                    Ext.checkRequired(value.toString(), displayName);
                }
                if (field.isAnnotationPresent(Email.class)) {
                    Ext.checkEmail(value.toString(), displayName);
                }
                if (field.isAnnotationPresent(Mobile.class)) {
                    Ext.checkMobile(value.toString(), displayName);
                }
                if (field.isAnnotationPresent(Length.class)) {
                    Length length = field.getAnnotationsByType(Length.class)[0];
                    Ext.checkLength(value.toString(), displayName, length.length());
                }
                if (field.isAnnotationPresent(Range.class)) {
                    Range range = field.getAnnotationsByType(Range.class)[0];
                    Ext.checkRange(value.toString(), displayName, range.minValue(), range.maxValue());
                }
            }
        }
    }
}