package com.example.demo.wxpay;

import lombok.Data;

/**
 * 微信支付需要的一些参数
 * @author chenp
 *
 */
@Data//此注解代替了set、get方法，如果不想用这个注解也可以自己写set、get方法。
public class WeChatParams {

    public String total_fee;//订单金额【备注：以分为单位】
    public String body;//商品名称
    public String out_trade_no;//商户订单号
    public String attach;//附加参数
    public String memberid;//会员ID


}