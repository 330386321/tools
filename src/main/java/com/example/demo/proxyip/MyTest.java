package com.example.demo.proxyip;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class MyTest {

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     * @throws UnsupportedEncodingException
     */
    public static String sendGet(String url){
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException{
        try {
            //获取代理ip
            ProxyCralwerUnusedVPN proxyCrawler = new ProxyCralwerUnusedVPN();
            //想要获取的代理IP个数，由需求方自行指定。（如果个数太多，将导致返回变慢）
            String ipresult =  proxyCrawler.startCrawler(1);
            if(!("").equals(ipresult)&&ipresult.length()>2){
                String[] iphost = ipresult.split(",");
                System.out.println("获取IP------>"+iphost[0]);
                System.out.println("获取IP------>"+iphost[1]);
                // 如果不设置，只要代理IP和代理端口正确
                System.getProperties().setProperty("http.proxyHost", iphost[0]);
                System.getProperties().setProperty("http.proxyPort", iphost[1]);
            }else{
                // 如果不设置，只要代理IP和代理端口正确
                System.getProperties().setProperty("http.proxyHost", "58.252.6.165");
                System.getProperties().setProperty("http.proxyPort", "9000");
            }

            //获取地震数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String getbegin = "2015-04-25 01:00:00";
            String begin = getbegin.substring(0,10);
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(getbegin));
            //设置当前日期
            c.add(Calendar.YEAR, 1); //间隔时间
            String end = sdf.format(c.getTime()).substring(0,10);
            //获取地震历史数据
            String url = "http://www.ceic.ac.cn/ajax/search?page=1&&start="+begin+"&&end="+end+"&&jingdu1=&&jingdu2=&&weidu1=&&weidu2=&&height1=&&height2=&&zhenji1=&&zhenji2=";
            String result = sendGet(url);
            System.out.println(result);
            if(result!=null&&!("").equals(result)){
                String val = StringUtils.substringBeforeLast(result.substring(1), ")");
                JSONObject jsStr = JSONObject.parseObject(val);
                JSONArray shuju = jsStr.getJSONArray("shuju");
                Date oldhappenTime = sdf.parse(getbegin); //上次存储的最晚地震发生时刻
                Date newbeginTime = sdf.parse(getbegin); //记录新的最晚地震发生时刻（存到Redis中用于记录）
                for (int i = 0; i < shuju.size(); i++) {
                    JSONObject js = shuju.getJSONObject(i);
                    Date happenTime = sdf.parse(js.getString("O_TIME")); //地震发生时刻
                    String name = js.getString("LOCATION_C");//名称
                    if(happenTime.after(oldhappenTime)){
                        System.out.println(sdf.format(happenTime)+name);
                        //记录新的最晚地震发生时刻（存到Redis中用于记录）
                        if(happenTime.after(newbeginTime)){
                            newbeginTime=happenTime;//交换值
                        }
                    }
                }
                System.out.println("最晚地震时刻："+sdf.format(newbeginTime));
                System.out.println(shuju.size());
                System.out.println(end);

            }

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }
}

