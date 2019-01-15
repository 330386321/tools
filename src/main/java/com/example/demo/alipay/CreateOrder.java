package com.example.demo.alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateOrder {

    /**
     *@param userId        充值人
     *@param tradeMoney    充值money(RMB)
     *@throws AlipayApiException  ModelAndView
     */
    @RequestMapping(value="api/alipay/createOrder",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Model alipay(
            @RequestParam("userId")String userId,
            @RequestParam("tradeMoney")String tradeMoney,Model m) throws AlipayApiException {

        String orderStr = "";
        try {

            /****** 1.封装你的交易订单开始 *****/                                        //自己用

            //此处封装你的订单数据，订单状态可以设置为等待支付

            /****** 1.封装你的交易订单结束 *****/

            Map<String,String> orderMap = new LinkedHashMap<String,String>();            //订单实体
            Map<String,String> bizModel = new LinkedHashMap<String,String>();            //公共实体

            /****** 2.商品参数封装开始 *****/                                            //手机端用
            // 商户订单号，商户网站订单系统中唯一订单号，必填
            //orderMap.put("out_trade_no",trade.getOrderNumber());
            // 订单名称，必填
            orderMap.put("subject","手机网站支付购买游戏币");
            // 付款金额，必填
            orderMap.put("total_amount",tradeMoney);
            // 商品描述，可空
            orderMap.put("body","您购买游戏币"+tradeMoney +"元");
            // 超时时间 可空
            orderMap.put("timeout_express","30m");
            // 销售产品码 必填
            orderMap.put("product_code","QUICK_WAP_PAY");

            /****** 2.商品参数封装结束 *****/

            /******--------------- 3.公共参数封装 开始 ------------------------*****/        //支付宝用
            //1.商户appid
            bizModel.put("app_id",AlipayConfig.APPID);
            //2.请求网关地址
            bizModel.put("method",AlipayConfig.URL);
            //3.请求格式
            bizModel.put("format",AlipayConfig.FORMAT);
            //4.回调地址
            bizModel.put("return_url",AlipayConfig.return_url);
            //5.私钥
            bizModel.put("private_key",AlipayConfig.RSA_PRIVATE_KEY);
            //6.商家id
            bizModel.put("seller_id","2088102170411333");
            //7.加密格式
            bizModel.put("sign_type",AlipayConfig.SIGNTYPE+"");

            /******--------------- 3.公共参数封装 结束 ------------------------*****/

            //实例化客户端
            AlipayClient client = new DefaultAlipayClient(AlipayConfig.URL, AlipayConfig.APPID, AlipayConfig.RSA_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);

            //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
            AlipayTradeAppPayRequest ali_request = new AlipayTradeAppPayRequest();

            //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setPassbackParams(URLEncoder.encode((String)orderMap.get("body").toString()));;  //描述信息  添加附加数据
            model.setBody(orderMap.get("body"));                        //商品信息
            model.setSubject(orderMap.get("subject"));                  //商品名称
            model.setOutTradeNo(orderMap.get("out_trade_no"));          //商户订单号(自动生成)
            model.setTimeoutExpress(orderMap.get("timeout_express"));     //交易超时时间
            model.setTotalAmount(orderMap.get("total_amount"));         //支付金额
            model.setProductCode(orderMap.get("product_code"));         //销售产品码
            model.setSellerId("20881021********");                        //商家id
            ali_request.setBizModel(model);
            ali_request.setNotifyUrl(AlipayConfig.notify_url);          //回调地址

            AlipayTradeAppPayResponse response = client.sdkExecute(ali_request);
            orderStr = response.getBody();
            System.err.println(orderStr);                                //就是orderString 可以直接给客户端请求，无需再做处理。

            m.addAttribute("result",orderStr);
            m.addAttribute("status",0);
            m.addAttribute("msg","订单生成成功");

        } catch (Exception e) {
            m.addAttribute("status",1);
            m.addAttribute("msg","订单生成失败");
        }

        return m;
    }
}
