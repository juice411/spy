package com.juice.spy.his.core;


import com.juice.spy.entity.Stock;
import com.juice.spy.statics.Stocks;
import com.juice.spy.util.DateUtil;
import com.juice.spy.util.Holiday;
import com.juice.spy.util.HttpTookit;
import org.apache.commons.lang.StringUtils;
import org.hbase.async.PutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchDatas {
    private static final Logger LOG = LoggerFactory.getLogger(FetchDatas.class);
    private static String url_his_ifeng = "http://api.finance.ifeng.com/akdaily/?code=#&type=last";

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");//不支持中文

    private final static Pattern p_his_comma = Pattern.compile("-?\"\\d{1,3},\\d.*?\"");


    public static void fetchHis(Stock stock) {
        fetchHis(Holiday.getNextWorkDay(Calendar.getInstance(),-1),stock);
    }

    public static void fetchHis(Calendar date,Stock stock) {


            String his = HttpTookit.doGet(url_his_ifeng.replace("#", stock.getCode()), null, "gbk", true);
            LOG.debug(his);

            if (StringUtils.isNotBlank(his)) {
                Pattern p_his = Pattern.compile("\\[(\"(#).*?)\\]".replace("#", DateUtil.Date2String(date.getTime(),"yyyy-MM-dd")));
                Matcher m = p_his.matcher(his);
                while (m.find()) {
                    LOG.debug("his:{}", m.group(1));
                    //将数据写入hbase
                    byte[] rowkey = stock.getCode().getBytes(CHARSET);
                    //去掉双引号之前先找出数字里含有逗号的数字，将其逗号替换掉
                    String his_comma=m.group(1);
                    Matcher m_comma = p_his_comma.matcher(his_comma);
                    StringBuffer sb = new StringBuffer();
                    while (m_comma.find()) {
                        // 将匹配之前的字符串复制到sb,再将匹配结果替换为："1293899.55"，并追加到sb
                        m_comma.appendReplacement(sb, m_comma.group(0).replaceAll(",",""));
                    }
                    m_comma.appendTail(sb);
                    LOG.debug(sb.toString());

                    byte[] value = sb.toString().replaceAll("\"", "").getBytes(CHARSET);

                    //这是获取当前是当年的第几天
                    byte[] qualifier = (date.get(Calendar.DAY_OF_YEAR) + "").getBytes(CHARSET);
                    PutRequest put = new PutRequest("stock-his".getBytes(CHARSET), rowkey, "t".getBytes(CHARSET), qualifier, value);
                    Stocks.getHclient().put(put);
                }

            }


    }

    static class SyncTask implements Runnable {
        private Stock stock;
        public SyncTask(Stock stock){
            this.stock=stock;
        }
        @Override
        public void run() {
            fetchHis(stock);
        }
    }

    public static void main(String[] args) {
        List<Stock> subList=new ArrayList<Stock>();
        /*for(int i=0;i<Stocks.getStocks().size();i++){
            if(Stocks.getStocks().get(i).getCode().equalsIgnoreCase("sz000628")){
                subList=Stocks.getStocks().subList(i,Stocks.getStocks().size());
            }
        }*/

        final Stack<Stock> st = new Stack();
        if(subList.size()>0){
            st.addAll(subList);
        }else {
            st.addAll(Stocks.getStocks());
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 10000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(100));
        while (st.size()>0){
            Stock stock=st.pop();
            executor.execute(new SyncTask(stock));
            LOG.info("即将抓取{},{}",stock.getCode(),stock.getName());
            LOG.info("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+executor.getQueue().size()+"，finished："+executor.getCompletedTaskCount());
            try {
                Thread.sleep(500);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(5);
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                try {

                    fetchHis(st.pop());
                    LOG.info("还剩余{}条待抓取,当前抓取到{}",st.size(),st.get(0));

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    LOG.error("-----------------{}----------------", e.getMessage());
                }
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);*/


    }

}
