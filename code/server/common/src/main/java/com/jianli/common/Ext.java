package com.jianli.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.domain.BaseDomain;
import com.jianli.common.exception.BadRequestException;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  基本数据类型工具类
 */

public class Ext {

    /**
     *  将对象转换成Boolean类型，默认为false
     * @param src，源对象
     * @return  Boolean，Boolean类型对象
     **/
    public static Boolean toBoolean(Object src) {
        return toBoolean(src, new Boolean(false));
    }

    /**
     *  将对象转换成Boolean类型
     * @param src，源对象
     * @param defaultValue，默认值
     * @return  Boolean，Boolean类型对象
     **/
    public static Boolean toBoolean(Object src, Boolean defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        if (src.toString().toLowerCase().equals("true") || src.toString().trim().equals("1")) {
            return true;
        }
        return false;
    }

    /**
     *  将对象转换成Integer类型，默认为空
     * @param src，源对象
     * @return  Integer，Integer类型对象
     **/
    public static Integer toInteger(Object src) {
        return toInteger(src, null);
    }

    /**
     *  将对象转换成Integer类型
     * @param src，源对象
     * @param defaultValue，默认值
     * @return  Integer，Integer类型对象
     **/
    public static Integer toInteger(Object src, Integer defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        Integer value = defaultValue;
        value = Integer.parseInt(src.toString());
        return value;
    }

    /**
     *  将对象转换成Long类型，默认为空
     * @param src，源对象
     * @return  Long，Long类型对象
     **/
    public static Long toLong(Object src) {
        return toLong(src, null);
    }

    public static Long toLong(Object src, Long defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        Long value = defaultValue;
        value = Long.parseLong(src.toString());
        return value;
    }

    /**
     *  将对象转换成IFloat类型，默认为空
     * @param src，源对象
     * @return  Float，Float类型对象
     **/
    public static Float toFloat(Object src) {
        return toFloat(src, null);
    }

    public static Float toFloat(Object src, Float defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        Float value = defaultValue;
        value = Float.parseFloat(src.toString());
        return value;
    }

    /**
     *  将对象转换成Double类型，默认为空
     * @param src，源对象
     * @return  Double，Double类型对象
     **/
    public static Double toDouble(Object src) {
        return toDouble(src, null);
    }

    public static Double toDouble(Object src, Double defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        Double value = defaultValue;
        value = Double.parseDouble(src.toString());
        return value;
    }

    /**
     *  将对象转换成BigDecimal类型，默认为空
     * @param src，源对象
     * @return  BigDecimal，BigDecimal类型对象
     **/
    public static BigDecimal toBigDecimal(Object src) {
        return toBigDecimal(src, null);
    }

    public static BigDecimal toBigDecimal(Object src, BigDecimal defaultValue) {
        if (isNullOrEmpty(src)) {
            return defaultValue;
        }
        BigDecimal value = defaultValue;
        value = BigDecimal.valueOf(toDouble(src));
        return value;
    }

    /**
     *  将Map对象转换成JSONObject对象
     * @param src，源对象
     * @return  JSONObject对象
     **/
    public static JSONObject toJObject(LinkedHashMap<String, Object> src) {
        JSONObject object = new JSONObject(true);
        if(src != null) {
            for (Map.Entry<String, Object> entry : src.entrySet()) {
                object.put(entry.getKey(), entry.getValue());
            }
        }
        return object;
    }

    public static JSONObject toJObject(Map<String, String> src) {
        JSONObject object = new JSONObject(true);
        if(src != null) {
            for (Map.Entry<String, String> entry : src.entrySet()) {
                object.put(entry.getKey(), entry.getValue());
            }
        }
        return object;
    }

    public static JSONObject toJObject(String src) {
        if (!isNullOrEmpty(src) && src.startsWith("{") && src.endsWith("}")) {
            return JSONObject.parseObject(src);
        }
        return new JSONObject(true);
    }

    /**
     *  将List<Map<String, Object>对象转换成JSONArray对象
     * @param src，源对象
     * @return  JSONArray对象
     **/
    public static JSONArray toJArray(List<LinkedHashMap<String, Object>> src) {
        JSONArray list = new JSONArray();
        for (LinkedHashMap<String, Object> o : src) {
            if(o != null) {
                list.add(toJObject(o));
            }
        }
        return list;
    }

    public static JSONObject find(JSONArray items, String name, Object value) {
        for (int i = 0; i < items.size(); ++i) {
            JSONObject item = items.getJSONObject(i);
            if (item.get(name).equals(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     *  取得当前时间
     **/
    public static Date now(){
       return  new Date();
    }



    /**
     *  将对象转换成Date类型，要求源对象的格式是yyyy-MM-dd HH:mm:ss
     * @param src，源对象
     * @return  Date对象
     **/
    public static Date toDate(Object src) throws Exception {
        if (isNullOrEmpty(src)) {
            return null;
        }
        return toDate(src.toString(), "yyyy-MM-dd HH:mm:ss");
    }


    /**
     *  将对象转换成Date类型，源对象的格式可以自定义
     * @param src，源对象
     * @return  Date对象
     **/
    public static Date toDate(String src, String format) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(src);
        return date;
    }

    /**
     *  根据年、月、日得到Date对象
     * @param  year，年
     * @param month，月
     * @param day，日
     * @return  Date对象
     **/
    public static Date toDate(int year, int month, int day) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = simpleDateFormat.parse(year + "-" + month + "-" + day);
        return date;
    }

    /**
     *  转换日期显示格式
     * @param  strDate，日期字符串
     * @param format，需要显示的格式
     * @return  String, 日期字符串
     **/
    public static String toDateString(String strDate, String format) throws Exception {
        if (strDate != null && strDate.length() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            Date date = dateFormat.parse(strDate);
            return toDateString(date, format);
        }
        return "";
    }

    /**
     *  转换日期显示格式
     * @param  date，日期对象
     * @param format，需要显示的格式
     * @return  String, 日期字符串
     **/
    public static String toDateString(Date date, String format) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     *  取当前年
     * @return  int, 年
     **/
    public static int getYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     *  日期取年
     * @return  int, 年
     **/
    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     *  取当前月
     * @return  int, 月
     **/
    public static int getMonth() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        if (month > 12) {
            month -= 12;
        }
        return month;
    }

    /**
     *  日期取月
     * @return  int, 月
     **/
    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH) + 1;
        if (month > 12) {
            month -= 12;
        }
        return month;
    }

    /**
     *  取当前日
     * @return  int, 日
     **/
    public static int getDay() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DATE);
        return day;
    }

    /**
     *  日期取日
     * @return  int, 日
     **/
    public static int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DATE);
        return day;
    }


    /**
     *  根据日期取得星期
     * @return  int, 星期
     **/
    public static int getDayOfWeek(Date date) {
        int[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }


    static String[] dayOfWeekTexts = {"", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    /**
     *  根据日期取得星期中文
     * @return  String, 星期中文
     **/
    public static String getDayOfWeekText(Date date) {
        int dayOfWeek = getDayOfWeek(date);
        if (dayOfWeek <= 7) {
            return dayOfWeekTexts[dayOfWeek];
        }
        return "";
    }

    /**
     *  根据星期数字取得星期中文
     * @param dayOfWeek，星期
     * @return  String, 星期中文
     **/
    public static String getDayOfWeekText(int dayOfWeek) {
        if (dayOfWeek <= 7) {
            return dayOfWeekTexts[dayOfWeek];
        }
        return "";
    }

    /**
     *  取得指定月份的前几个月份
     * @param month，当前月份
     * @param number，需要往前取的月份数
     * @return  int, 月份
     **/
    public static int getPreMonth(int month, int number) {
        int preMonth = month;
        for (int i = 0; i < number; ++i) {
            if (preMonth < 1) {
                preMonth = 12;
            } else {
                preMonth -= 1;
            }
        }
        return preMonth;
    }

    /**
     *  取得指定月份的天数
     * @param year，年份
     * @param month，月份
     * @return  int, 天数
     **/
    public static int getMonthDayNumber(int year, int month) throws Exception {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        calendar.setTime(format.parse(year + "-" + month));
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     *  取得当前月份的第一天日期
     * @return  Date
     **/
    public static Date getFirstDayOfMonth() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String firstday;
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        firstday = format.format(cale.getTime());
        return toDate(firstday, "yyyy-MM-dd");
    }

    /**
     *  取得指定月份的第一天日期
     * @param year，年份
     * @param month，月份
     * @return  Date
     **/
    public static Date getFirstDayOfMonth(int year, int month) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = sdf.format(cal.getTime());
        return toDate(firstDayOfMonth, "yyyy-MM-dd");
    }

    /**
     *  取得当前月份的最后一天日期
     * @return  Date
     **/
    public static Date getLastDayOfMonth() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        int lastDay = cale.getActualMaximum(Calendar.DAY_OF_MONTH);
        cale.set(Calendar.DAY_OF_MONTH, lastDay);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cale.getTime());
        return toDate(lastDayOfMonth, "yyyy-MM-dd");
    }

    /**
     *  取得指定月份的最后一天日期
     * @param year，年份
     * @param month，月份
     * @return  Date
     **/
    public static Date getLastDayOfMonth(int year, int month) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return toDate(lastDayOfMonth, "yyyy-MM-dd");
    }

    /**
     *  取得指定日期的后几天日期
     * @param date，日期
     * @param n，天数
     * @return  Date
     **/
    public static Date addDays(Date date, int n) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, n);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String stringDate = sdf.format(cal.getTime());
        return toDate(stringDate, "yyyy-MM-dd");
    }


    private static String[] emptyStrings = {"", "0", "00", "000", "0000", "00000", "000000", "0000000", "00000000", "000000000"};

    /**
     *  取得指定长度的字符串，左边以0补齐
     * @param src，源字符串
     * @param length，长度
     * @return  Date
     **/
    public static String getString(String src, int length) {
        if (src.length() > length) {
            return src;
        }
        int number = length - src.length();
        if (number >= 0 && number < emptyStrings.length) {
            return emptyStrings[number] + src;
        }
        return src;
    }

    /**
     *  判断数字是否相等
     * @param a，数字1
     * @param b, 数据2
     * @return  是否相等
     **/
    public static Boolean equal(double a, double b) {
        if (Math.abs(a - b) < 0.0000001) {
            return true;
        }
        return false;
    }

    /**
     *  判断字符串是否为空
     * @return  是否为空
     **/
    public static Boolean isNullOrEmpty(Object src) {
        if (src == null || src.toString().trim().length() < 1) {
            return true;
        }
        return false;
    }

    /**
     *  判断数字是否为空或为0
     * @return  是否为空或为0
     **/
    public static Boolean isNullOrZero(Long src) {
        if (src == null || src < 1) {
            return true;
        }
        return false;
    }

    /**
     *  字符串MD5加密
     * @param src， 源字符串
     * @return  加密后的字符串
     **/
    public static String md5(String src) {
        String base = src;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5.toUpperCase();
    }

    /**
     *  字符串base64加密
     * @param src， 源字符串
     * @return  加密后的字符串
     **/
    public static String base64Encode(String src) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(src.getBytes());
    }

    /**
     *  字符串base64解密
     * @param src， 源字符串
     * @return  解密后的字符串
     **/
    public static String base64Decode(String src) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(src);
        return new String(bytes);
    }

    /**
     *  取得UUID
     * @return  UUID
     **/
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }


    /**
     *  取得指定长度的随机数字字符串
     * @return  String, 随机数字字符串
     **/
    public static String getRandomString(int length) {
        String input = getDigit(UUID.randomUUID().toString());
        if (input.length() < length) {
            input += (UUID.randomUUID().toString());
        }
        if (input.length() > length) {
            return input.substring(0, length);
        }
        return input;
    }

    /**
     *  取得字符串中的数字部分
     * @return  String, 数字字符串
     **/
    public static String getDigit(String src) {
        return src.replaceAll("[^0-9]", "");
    }


    /**
     *  取得内部的异常对象
     * @param ex， 异常对象
     * @return  Exception, 内部异常对象
     **/
    public static Exception getInnerException(Exception ex) {
        Exception e = ex;
        if (ex instanceof InvocationTargetException) {
            InvocationTargetException invocationTargetException = (InvocationTargetException) ex;
            if (invocationTargetException != null) {
                Exception innerEx = (Exception) invocationTargetException.getTargetException();
                if (innerEx != null) {
                    e = innerEx;
                }
            }
        }
        return e;
    }


    /**
     *   验证必填
     * @param value， 值
     * @param displayName， 显示名
     */
    public static void checkRequired(Object value, String displayName) throws Exception {
        if (isNullOrEmpty(value)) {
            throw new BadRequestException(displayName + "不能为空");
        }
    }

    /**
     *   验证长度
     * @param value， 值
     * @param displayName， 显示名
     * @param length， 长度
     */
    public static void checkLength(String value, String displayName, Integer length) throws Exception {
        if (!isNullOrEmpty(value)) {
            if(value.length() > length)
            {
                throw new BadRequestException(displayName + "的长度不能超过" + length.toString());
            }
        }
    }

    /**
     *   验证邮箱格式
     * @param value， 值
     * @param displayName， 显示名
     */
    public static void checkEmail(String value, String displayName) throws Exception {
        if (!isNullOrEmpty(value)) {
            boolean ok = false;
            if(value.contains("@")&&value.contains(".")) {
                if(value.lastIndexOf(".")>value.lastIndexOf("@")) {
                    ok = true;
                }
            }
            if(!ok)
            {
                throw new BadRequestException(displayName + "的邮箱格式不正确");
            }
        }
    }

    /**
     *   验证手机号格式
     * @param value， 值
     * @param displayName， 显示名
     */
    public static void checkMobile(String value, String displayName) throws Exception {
        if (!isNullOrEmpty(value)) {
            String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
            if (value.length() != 11) {
            throw new BadRequestException(displayName + "的长度应该是11位");
            } else {
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(value);
                boolean isMatch = m.matches();
                if (!isMatch) {
                   throw new BadRequestException("请填入正确的" + displayName);
                }
            }
        }
    }

    /**
     *   验证数据范围
     * @param value， 值
     * @param displayName， 显示名
     * @param minValue， 最小值
     * @param maxValue， 最大值
     */
    public static void checkRange(String value, String displayName, Integer minValue, Integer maxValue)
    {
        if (!isNullOrEmpty(value))
        {
            Integer tempValue = toInteger(value);
            if (tempValue < minValue || tempValue > maxValue)
            {
                throw new BadRequestException(displayName + "的值必须在" + minValue +  "和" + maxValue + "之间");
            }
        }
    }


    /**
     *   获取Json段字段
     * @param json， json对象
     * @param key， 字段名
     * @return  返回字段值
     */
    public  static  String getString(JSONObject json, String key)
    {
        String value = "";
        if(json.containsKey(key) && !isNullOrEmpty(json.get(key)))
        {
            value = json.getString(key);
        }
        return value;
    }

    /**
     *   判断字符串是否包含中文字符
     * @param src， json对象
     * @return  是否包含中文字符
     */
    public static boolean isChinese(String src)
    {
        Boolean isChinese = false;
        char[] chars = src.toCharArray();
        for (char c : chars)
        {
            if ((int)c > 255)
            {
                isChinese = true;
                break;
            }
        }
        return isChinese;
    }

    /**
     *   将字字符转成大写
     * @param c， 字符
     * @return  是否包含中文字符
     */
    public static char toUpper(char c) {
        if (c >= 'a' && c <= 'z') {
            c -= 32;
        }
        return c;
    }

    /**
     *   获取文件扩展名
     * @param fileName， 文件名
     * @return  返回文件扩展名
     */
    public  static String getFileExtName(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return null;
        }
        String ext = fileName.substring(index + 1);
        return ext;
    }

    public static String getRandomFileName(String fileName) throws  Exception
    {
        Date date = new Date();
        return Ext.toDateString(date, "yyyyMMddHHmmss") + getRandomString(3) + "." + getFileExtName(fileName);
    }


    public static String byte2Base64(byte[] b){
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(b);
    }


    public static  byte[]  base642Byte(String src){
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(src);
        return bytes;
    }


    //数组转字符串
    public static String join(Object [] items, String separator) {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<items.length; ++i) {
            if(i>0) {
                stringBuffer.append(separator);
            }
            stringBuffer.append(items[i]);
        }
        return stringBuffer.toString();
    }

}