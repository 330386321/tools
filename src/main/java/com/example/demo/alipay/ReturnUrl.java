package com.example.demo.alipay;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ReturnUrl {

    /**
     * 支付宝支付成功后.通知页面
     *@author Zhao
     *@date 2017年11月2日
     *@param request
     *@return
     *@throws UnsupportedEncodingException
     */
    @RequestMapping(value="api/alipay/return_url",method={RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public Model returnUrl(@RequestParam("id") String id, HttpServletRequest request, Model model) throws UnsupportedEncodingException {/*
        System.err.println("。。。。。。 同步通知 。。。。。。");
        System.err.println("。。。。。。 同步通知 。。。。。。");
        System.err.println("。。。。。。 同步通知 。。。。。。");
        Map returnMap = new HashMap();
        try {

            Trade trade = tradeService.selectByOrderNumber(id);
            // 返回值Map
            if(trade !=null && trade.getTradeStatus() == 2){
                User user = userService.selectByPrimaryKey(trade.gettUserId());
                returnMap.put("tradeType", trade.getTradeType());             //支付方式
                returnMap.put("phoneNum", user.getPhoneNumber());             //支付帐号
                returnMap.put("tradeMoney", trade.getTradeMoney()+"");        //订单金额

            }else{
                model.addAttribute("msg", "查询失败");
                model.addAttribute("status", 0);
            }
            model.addAttribute("returnMap", returnMap);
            System.err.println(returnMap);
            model.addAttribute("msg", "查询成功");
            model.addAttribute("status", 0);
        } catch (Exception e) {
            model.addAttribute("msg", "查询失败");
            model.addAttribute("status", 1);
        }
*/
        return model;
    }
}
