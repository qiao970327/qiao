package com.fh.shop;

import com.fh.shop.config.WXConfig;
import com.github.wxpay.sdk.WXPay;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class WxTest {

    @Test
    public void test1() throws Exception {

       WXConfig config = new WXConfig();
       WXPay wxpay = new WXPay(config);

        Map<String, String> data = new HashMap<String, String>();
        data.put("body", "飞狐乐购-订单支付");
        data.put("out_trade_no", "32332");
        data.put("total_fee", "10000000");
        data.put("notify_url", "http://www.example.com/wxpay/notify");
        data.put("trade_type", "NATIVE");  // 此处指定为扫码支付

        try {
            Map<String, String> resp = wxpay.unifiedOrder(data);
            System.out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void test2() throws Exception {

        WXConfig config = new WXConfig();
        WXPay wxpay = new WXPay(config);

        Map<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", "32332");

        try {
            Map<String, String> resp = wxpay.orderQuery(data);
            System.out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
