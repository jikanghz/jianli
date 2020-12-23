package com.jianli.common.dao;

import java.util.HashMap;

public class CharSql {
    private class CharItem
    {
        public int start;
        public int end;

        public CharItem(int s, int e)
        {
            start = s;
            end = e;
        }
    }

    private HashMap<String, CharItem> items;

    public CharSql()
    {
        items = new HashMap<String, CharItem>();

        items.put("A", new CharItem(45217, 45252));
        items.put("B", new CharItem(45253, 45760));
        items.put("C", new CharItem(45761, 46317));
        items.put("D", new CharItem(46318, 46825));
        items.put("E", new CharItem(46826, 47009));
        items.put("F", new CharItem(47010, 47296));
        items.put("G", new CharItem(47297, 47613));
        items.put("H", new CharItem(47614, 48118));

        items.put("J", new CharItem(48119, 49061));
        items.put("K", new CharItem(49062, 49323));
        items.put("L", new CharItem(49324, 49895));
        items.put("M", new CharItem(49896, 50370));
        items.put("N", new CharItem(50371, 50613));
        items.put("O", new CharItem(50614, 50621));
        items.put("P", new CharItem(50622, 50905));
        items.put("Q", new CharItem(50906, 51386));
        items.put("R", new CharItem(51387, 51445));
        items.put("S", new CharItem(51446, 52217));
        items.put("T", new CharItem(52218, 52697));

        items.put("W", new CharItem(52698, 52979));
        items.put("X", new CharItem(52980, 53640));
        items.put("Y", new CharItem(53689, 54480));
        items.put("Z", new CharItem(54481, 55289));
    }

    public String start(char c)
    {
        String key = String.valueOf(c);
        if(items.containsKey(key))
        {
            return  String.valueOf(items.get(key).start);
        }
        return "";
    }

    public String end(char c)
    {
        String key = String.valueOf(c);
        if(items.containsKey(key))
        {
            return  String.valueOf(items.get(key).end);
        }
        return "";
    }
}
