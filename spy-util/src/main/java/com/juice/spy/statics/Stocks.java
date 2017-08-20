package com.juice.spy.statics;

import com.juice.spy.entity.Stock;
import org.hbase.async.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Stocks {
    private static final Logger LOG = LoggerFactory.getLogger(Stocks.class);
    private static final Charset CHARSET = Charset.forName("ISO-8859-1");//不支持中文
    private static final String REGEX_CHINESE = "[\\u4e00-\\u9fa5]";
    private static final Pattern PATTERN_CHINESE = Pattern.compile(REGEX_CHINESE);
    private final static HBaseClient hclient = new HBaseClient("h8,h6,h181");
    private final static List<Stock> stocks = new ArrayList<Stock>();

    static {
        final Scanner scanner = hclient.newScanner("stock");
        scanner.setStartKey(toBytes(""));
        scanner.setFamily("t");
        scanner.setQualifier("name".getBytes(CHARSET));
        ArrayList<ArrayList<KeyValue>> rows;

        try {
            while ((rows = scanner.nextRows().join()) != null) {
                for (final ArrayList<KeyValue> row : rows) {
                    String key = fromBytes(row.get(0).key());
                    String name=fromBytes(row.get(0).value());
                    Stock stock = new Stock();
                    stock.setName(name);
                    stock.setCode(key);
                    stocks.add(stock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(stocks.size()==0){
            readTxtFile("E:\\spy\\spy-util\\src\\main\\resources\\sz.txt", "sz");
            readTxtFile("E:\\spy\\spy-util\\src\\main\\resources\\sh.txt", "sh");

            addStock(stocks);
        }
    }

    public static void readTxtFile(String filePath, String prefix) {

        try {
            String encoding = "utf-8";
            Pattern p_code = Pattern.compile("([\\u4e00-\\u9fa5].*?)\\((\\d.*?)\\)");
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    Matcher m = p_code.matcher(lineTxt);
                    while (m.find()) {
                        Stock stock=new Stock();
                        stock.setCode(prefix + m.group(2));
                        stock.setName(m.group(1));
                        stocks.add(stock);
                    }
                    m = null;
                }
                read.close();
            } else {
                LOG.error("找不到指定的文件");
            }
        } catch (Exception e) {
           LOG.error("读取文件内容出错");
            e.printStackTrace();
        }

    }

    public static void addStock(List<Stock>stockList){
        for(Stock stock:stockList){

            PutRequest put = new PutRequest("stock".getBytes(CHARSET), stock.getCode().getBytes(CHARSET), "t".getBytes(CHARSET), "name".getBytes(CHARSET), toBytes(stock.getName()));
            hclient.put(put);
        }

    }
    public static HBaseClient getHclient() {
        return hclient;
    }

    public static List<Stock> getStocks() {
        return stocks;
    }

    public static byte[] toBytes(final String s) {

        Matcher m = PATTERN_CHINESE.matcher(s);
        if (m.find()) {//有中文
            return s.getBytes(Charset.forName("UTF-8"));
        }

        return s.getBytes(CHARSET);
    }

    public static String fromBytes(final byte[] b) {

        String s = new String(b, Charset.forName("UTF-8"));
        if (b.length > s.length()) {//有中文
            return s;
        }
        return new String(b, CHARSET);
    }

    public static void main(String[] args) {
        LOG.debug("start");
    }
}
