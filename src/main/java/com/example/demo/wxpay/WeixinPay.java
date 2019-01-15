package com.example.demo.wxpay;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


public class WeixinPay {

    public static Logger lg=Logger.getLogger(WeixinPay.class);
    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 获取微信支付的二维码地址
     * @return
     * @author chenp
     * @throws Exception
     */
    public static String getCodeUrl(WeChatParams ps) throws Exception {
        /**
         * 账号信息
         */
        String appid = WeChatConfig.APPID;//微信服务号的appid
        String mch_id = WeChatConfig.MCHID; //微信支付商户号
        String key = WeChatConfig.APIKEY; // 微信支付的API密钥
        String notify_url = WeChatConfig.WECHAT_NOTIFY_URL_PC;//回调地址【注意，这里必须要使用外网的地址】
        String ufdoder_url=WeChatConfig.UFDODER_URL;//微信下单API地址
        String trade_type = "NATIVE"; //类型【网页扫码支付】

        /**
         * 时间字符串
         */
        String currTime = PayForUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = PayForUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        /**
         * 参数封装
         */
        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);//随机字符串
        packageParams.put("body", ps.body);//支付的商品名称
        packageParams.put("out_trade_no", ps.out_trade_no+nonce_str);//商户订单号【备注：每次发起请求都需要随机的字符串，否则失败。】
        packageParams.put("total_fee", ps.total_fee);//支付金额
        packageParams.put("spbill_create_ip", PayForUtil.localIp());//客户端主机
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);
        packageParams.put("attach", ps.attach);//额外的参数【业务类型+会员ID+支付类型】


        String sign = PayForUtil.createSign("UTF-8", packageParams,key);  //获取签名
        packageParams.put("sign", sign);

        String requestXML = PayForUtil.getRequestXml(packageParams);//将请求参数转换成String类型
        lg.info("微信支付请求参数的报文"+requestXML);
        String resXml = HttpUtil.postData(ufdoder_url,requestXML);  //解析请求之后的xml参数并且转换成String类型
        Map map = XMLUtil.doXMLParse(resXml);
        lg.info("微信支付响应参数的报文"+resXml);
        String urlCode = (String) map.get("code_url");

        return urlCode;
    }

    /**
     * 将路径生成二维码图片
     * @author chenp
     * @param content
     * @param response
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void encodeQrcode(String content,HttpServletResponse response){

        if(StringUtils.isBlank(content))
            return;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map hints = new HashMap();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 250, 250,hints);
            BufferedImage image = toBufferedImage(bitMatrix);
            //输出二维码图片流
            try {
                ImageIO.write(image, "png", response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (WriterException e1) {
            e1.printStackTrace();
        }
    }
    /**
     * 类型转换
     * @author chenp
     * @param matrix
     * @return
     */
    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) == true ? BLACK : WHITE);
            }
        }
        return image;
    }
    // 特殊字符处理
    public static String UrlEncode(String src)  throws UnsupportedEncodingException {
        return URLEncoder.encode(src, "UTF-8").replace("+", "%20");
    }



    //////////////////////////////
    /**
     * pc端微信支付之后的回调方法
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="wechat_notify_url_pc",method=RequestMethod.POST)
    public void wechat_notify_url_pc(HttpServletRequest request, HttpServletResponse response) throws Exception{

        //读取参数
        InputStream inputStream ;
        StringBuffer sb = new StringBuffer();
        inputStream = request.getInputStream();
        String s ;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null){
            sb.append(s);
        }
        in.close();
        inputStream.close();

        //解析xml成map
        Map<String, String> m = new HashMap<String, String>();
        m = XMLUtil.doXMLParse(sb.toString());

        //过滤空 设置 TreeMap
        SortedMap<Object,Object> packageParams = new TreeMap<Object,Object>();
        Iterator<String> it = m.keySet().iterator();
        while (it.hasNext()) {
            String parameter = it.next();
            String parameterValue = m.get(parameter);

            String v = "";
            if(null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }
        // 微信支付的API密钥
        String key = WeChatConfig.APIKEY; // key

        lg.info("微信支付返回回来的参数："+packageParams);
        //判断签名是否正确
        if(PayForUtil.isTenpaySign("UTF-8", packageParams,key)) {
            //------------------------------
            //处理业务开始
            //------------------------------
            String resXml = "";
            if("SUCCESS".equals((String)packageParams.get("result_code"))){
                // 这里是支付成功
                //执行自己的业务逻辑开始
                String app_id = (String)packageParams.get("appid");
                String mch_id = (String)packageParams.get("mch_id");
                String openid = (String)packageParams.get("openid");
                String is_subscribe = (String)packageParams.get("is_subscribe");//是否关注公众号

                //附加参数【商标申请_0bda32824db44d6f9611f1047829fa3b_15460】--【业务类型_会员ID_订单号】
                String attach = (String)packageParams.get("attach");
                //商户订单号
                String out_trade_no = (String)packageParams.get("out_trade_no");
                //付款金额【以分为单位】
                String total_fee = (String)packageParams.get("total_fee");
                //微信生成的交易订单号
                String transaction_id = (String)packageParams.get("transaction_id");//微信支付订单号
                //支付完成时间
                String time_end=(String)packageParams.get("time_end");

                lg.info("app_id:"+app_id);
                lg.info("mch_id:"+mch_id);
                lg.info("openid:"+openid);
                lg.info("is_subscribe:"+is_subscribe);
                lg.info("out_trade_no:"+out_trade_no);
                lg.info("total_fee:"+total_fee);
                lg.info("额外参数_attach:"+attach);
                lg.info("time_end:"+time_end);

                //执行自己的业务逻辑结束
                lg.info("支付成功");
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";

            } else {
                lg.info("支付失败,错误信息：" + packageParams.get("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            //------------------------------
            //处理业务完毕
            //------------------------------
            BufferedOutputStream out = new BufferedOutputStream(
                    response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } else{
            lg.info("通知签名验证失败");
        }

    }

}