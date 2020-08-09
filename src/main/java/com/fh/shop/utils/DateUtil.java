package com.fh.shop.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final String Y_M_D = "yyyy-MM-dd";
    public static final String S_F_M = "yyyy-MM-dd HH:mm:ss";
    public static final String FULLTIMEINFO = "yyyyMMddHHmmss";

    public static String addMinutes(Date date,int minute,String pattern){
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.MINUTE,minute);
        Date time = instance.getTime();
        return date2str(time,pattern);
    }

    public static String date2str(Date date , String pattern){
        if (date == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String format = sdf.format(date);
        return format;
    }

    public static Date str2date(String date, String pattern){
        if (StringUtils.isEmpty(date)){
            throw new RuntimeException("日期格式的字符串为空！");
        }
        SimpleDateFormat ss = new SimpleDateFormat(pattern);
        Date parse = null;
        try {
            parse = ss.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parse;
    }
}
