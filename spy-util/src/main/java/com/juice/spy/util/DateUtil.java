package com.juice.spy.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    /** 
     * 时间戳转换成日期格式字符串 
     * @param timeStamp 
     * @param format
     * @return 
     */
    public static String timeStamp2Date(String timeStamp, String format) {

        if (timeStamp == null || timeStamp.isEmpty() || timeStamp.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty())
            format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.format(new Date(Long.valueOf(timeStamp.length() == 10 ? timeStamp + "000" : timeStamp)));
    }

    /** 
     * 日期格式字符串转换成时间戳 
     * @param date_str 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss 
     * @return 
     */
    public static long date2TimeStamp(String date_str, String format) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Date string2Date(String date, String format) throws ParseException {

        if (format == null || format.isEmpty())
            format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.parse(date);
    }

    public static String Date2String(Date date, String format){

        if (format == null || format.isEmpty())
            format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        return sdf.format(date);
    }

    /** 
     * 取得当前时间戳（精确到秒） 
     * @return 
     */
    public static String timeStamp() {

        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }

    public static String currentDate(String format) {

        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    //  输出结果：  
    //  timeStamp=1417792627  
    //  date=2014-12-05 23:17:07  
    //  1417792627  
    public static void main(String[] args) {

        String timeStamp = timeStamp();
        System.out.println("timeStamp=" + timeStamp);

        String date = timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
        System.out.println("date=" + date);

    }

}
