package com.example.demo.wxpay;

import org.springframework.web.bind.annotation.RequestMapping;

public class Test {

    @RequestMapping("/wxPay")
    public String wxPay(WeChatParams ps) throws Exception {
        ps.setBody("测试商品3");
        ps.setTotal_fee("1");
        ps.setOut_trade_no("hw5409550792199899");
        ps.setAttach("xiner");
        ps.setMemberid("888");
        String urlCode = WeixinPay.getCodeUrl(ps);
        System.out.println(urlCode);
        return "";

    }


}
