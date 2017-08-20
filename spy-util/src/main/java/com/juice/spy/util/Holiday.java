package com.juice.spy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by juice on 2017/8/18.
 */
public class Holiday {
    private static final Logger LOG = LoggerFactory.getLogger(Holiday.class);
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static List<Calendar> holidayList = new ArrayList<Calendar>();  //节假日列表

    static {
        Holiday.initHolidayList("2017-10-01",8);
    }
    public static void main(String[] args) {
        try {

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Calendar ca = Calendar.getInstance();
            Date d = df.parse("2017-08-19");
            ca.setTime(d);//设置当前时间

            Holiday.initHolidayList("2017-08-19",3);//初始节假日

            Calendar ctmp=Calendar.getInstance();
            ctmp.setTime(df.parse("2017-08-21"));
            if(Holiday.checkHoliday(ctmp)){
                LOG.debug("{}是节假日",ctmp.getTime());
            }else{
                LOG.debug("{}是非节假日",ctmp.getTime());
            }

            int days=-6;
            Calendar c = Holiday.getNextWorkDay(ca,days);
            LOG.debug("{}后第{}个工作日期是{}",ca.getTime(),days,df.format(c.getTime()));

        } catch ( Exception e) {
            // TODO: handle exception
            System.out.println(e.getClass());
            e.printStackTrace();
        }

    }

    public static Calendar getNextWorkDay(Calendar calendar,int days){
        if(days>0){
            return addDateByWorkDay(calendar,days);
        }else if(days<0){
            return  subDateByWorkDay(calendar,days);
        }else
            return calendar;

    }


    /**
     *
     * <p>Title: addDateByWorkDay </P>
     * <p>Description: TODO  计算相加day天，并且排除节假日和周末后的日期</P>
     * @param calendar  当前的日期
     * @param days  相加天数
     * @return
     * return Calendar    返回类型   返回相加day天，并且排除节假日和周末后的日期
     * throws
     * date 2014-11-24 上午10:32:55
     */
    private static Calendar addDateByWorkDay(Calendar calendar,int days){
        Calendar c=Calendar.getInstance(Locale.CHINA);
        c.setTime(calendar.getTime());
        try {
            for (int i = 0; i < days; i++) {

                c.add(Calendar.DAY_OF_MONTH, 1);

                if(checkHoliday(c)){
                    i--;
                }
            }
            return c;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private static Calendar subDateByWorkDay(Calendar calendar,int days){
        Calendar c=Calendar.getInstance(Locale.CHINA);
        c.setTime(calendar.getTime());
        try {
            for (int i = 0; i > days; i--) {

                c.add(Calendar.DAY_OF_MONTH, -1);

                if(checkHoliday(c)){
                    i++;
                }
            }
            return c;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     *
     * <p>Title: checkHoliday </P>
     * <p>Description: TODO 验证日期是否是节假日</P>
     * @param calendar  传入需要验证的日期
     * @return
     * return boolean    返回类型  返回true是节假日，返回false不是节假日
     * throws
     * date 2014-11-24 上午10:13:07
     */
    public static boolean checkHoliday(Calendar calendar) throws Exception{

        //判断日期是否是周六周日
        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            return true;
        }
        //判断日期是否是节假日
        for (Calendar ca : holidayList) {
            if(ca.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    ca.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)&&
                    ca.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)){
                return true;
            }
        }

        return false;
    }

    /**
     *
     * <p>Title: initHolidayList </P>
     * <p>Description: TODO  把所有节假日放入list</P>
     * @param date  从数据库查 查出来的格式2014-05-09
     * return void    返回类型
     * throws
     * date 2014-11-24 上午10:11:35
     */
    public static void initHolidayList( String date){

        initHolidayList(date,1);
    }
    /**
     *
     * <p>Title: initHolidayList </P>
     * <p>Description: TODO  把所有节假日放入list</P>
     * @param date  从数据库查 查出来的格式2014-05-09
     * @param days  从date开始放几天假
     * return void    返回类型
     * throws
     * date 2014-11-24 上午10:11:35
     */
    public static void initHolidayList( String date,int days){

        /*String [] da = date.split("-");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.valueOf(da[0]));
        calendar.set(Calendar.MONTH, Integer.valueOf(da[1])-1);//月份比正常小1,0代表一月
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(da[2]));*/



        for(int i=0;i<days;i++){
            Calendar ca = Calendar.getInstance();
            Date d = null;
            try {
                d = df.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ca.setTime(d);
            ca.add(5,i);
            holidayList.add(ca);
        }

    }
}
