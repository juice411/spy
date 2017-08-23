package com.juice.spy.live.core;


import com.juice.spy.entity.Stock;
import com.juice.spy.statics.Stocks;
import com.juice.spy.util.Holiday;
import com.juice.spy.util.HttpTookit;
import com.juice.spy.util.VarianceAndStandardDiviation;
import org.apache.commons.lang.StringUtils;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Handle {
    private static final Logger LOG = LoggerFactory.getLogger(Handle.class);
    private static String url_live_qq = "http://qt.gtimg.cn/q=s_#";

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");//不支持中文


    public static void handle(Stock stock) {

            String live = HttpTookit.doGet(url_live_qq.replace("#", stock.getCode()), null, "gbk", true);
            LOG.debug(live);
            //解析实时
            if (StringUtils.isNotBlank(live) && !live.startsWith("v_pv_none_match")) {
                String[] live_stock = live.split("~");
                if (StringUtils.isNotBlank(live_stock[8])) {//股票状态，比如S,D等
                    return;
                }

                //读出昨天的历史数据与当前实时值做算法
                Calendar c = Holiday.getNextWorkDay(Calendar.getInstance(),-1);
                //这是获取当前是当年的第几天
                byte[] qualifier = (c.get(Calendar.DAY_OF_YEAR) + "").getBytes(CHARSET);
                //byte[] qualifier = "230".getBytes(CHARSET);
                GetRequest get = new GetRequest("stock-his".getBytes(CHARSET), stock.getCode().getBytes(CHARSET), "t".getBytes(CHARSET),qualifier);
                KeyValue kv=null;
                try {
                    kv= Stocks.getHclient().get(get).joinUninterruptibly(5000).get(0);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                String[] his=new String(kv.value(),CHARSET).split(",");

                if(his.length!=0){
                    //date open high close low chg p_chg ma5 ma10 ma20 vma5 vma10 vma20 turnover
                    double[] vma=new double[3];
                    vma[0]=Double.valueOf(his[11]);
                    vma[1]=Double.valueOf(his[12]);
                    vma[2]=Double.valueOf(his[13]);

                    double standard= VarianceAndStandardDiviation.StandardDiviation(vma);//成交量标准差
                    standard/=10000;//以万手作为单位
                    if(standard<3){//说明3条均量线交织在一起
                        //此时判断实时量能是否有效放大
                        if((Double.valueOf(live_stock[6])-vma[0])/vma[0]>=1.5){
                            LOG.debug("stock:{},{}量能异动",live_stock[1],live_stock[2]);
                        }
                    }
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
            handle(stock);
        }
    }


    public static void main(String[] args) {

        Calendar cal = Calendar.getInstance();
        cal.getTime();

        ThreadPoolExecutor executor=executor = new ThreadPoolExecutor(10, 50, 10000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(100));
        while (true){
            try {
                if (!Holiday.checkHoliday(cal)) {
                    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date curr = sdf.parse(sdf.format(new Date()));
                    Date d1 = sdf.parse("09:19:50");
                    Date d2 = sdf.parse("11:30:30");
                    Date d3 = sdf.parse("12:59:50");
                    Date d4 = sdf.parse("15:00:30");

                    if ((curr.after(d1) && curr.before(d2)) || (curr.after(d3) && curr.before(d4))) {
                        try {
                            for (Stock stock : Stocks.getStocks()) {
                                SyncTask syncTask=new SyncTask(stock);
                                executor.execute(syncTask);
                                try {
                                    Thread.sleep(1000);

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                LOG.debug("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+executor.getQueue().size()+"，finished："+executor.getCompletedTaskCount());
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            LOG.error("-----------------{}----------------", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
