package com.juice.spy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by juice on 2017/8/18.
 */
public class Test {
    private static final Logger LOG = LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) {
        String his="\"2015-11-12\",\"10.500\",\"11.540\",\"11.380\",\"10.210\",\"1826753.00\",\"0.890\",\"8.48\",\"10.598\",\"10.171\",\"10.380\",\"-1,063,244.45\",\"1,976,718.13\",\"1,293,899.55\",\"19.99\"";
        Pattern p_his = Pattern.compile("\"-?\\d{1,3},\\d.*?\"");
        Matcher m = p_his.matcher(his);
        /*while (m.find()){
            LOG.debug("{}",m.group(0));
        }
        m.reset();*/
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            // 将匹配之前的字符串复制到sb,再将匹配结果替换为："1293899.55"，并追加到sb
            m.appendReplacement(sb, m.group(0).replaceAll(",",""));
        }
        System.out.println(sb.toString());
        m.appendTail(sb);
        System.out.println(sb.toString());
    }
}
