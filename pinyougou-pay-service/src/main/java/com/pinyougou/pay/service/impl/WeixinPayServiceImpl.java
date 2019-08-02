package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid; //微信公众账号或开放平台 APP 的唯一标识
    @Value("${partner}")
    private String partner; //商户号 (配置文件中的 partner)
    @Value("${partnerkey}")
    private String partnerkey; //商户密钥
    @Value("${notifyurl}")
    private String notifyurl; //通知地址

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map<String, String> param = new HashMap<>(); //创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "tqFive");//商家描述
        param.put("out_trade_no", out_trade_no);//商户订单号
        param.put("total_fee", total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", notifyurl);//回调地址(随便写)
        param.put("trade_type", "NATIVE");

        //2.生成要发送的 xml
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(paramXml);
            //支付接口URL地址
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //是否https协议
            httpClient.setHttps(true);
            //设置参数
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获得结果
            String result = httpClient.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", total_fee);//总金额
            map.put("out_trade_no", out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String, String> param = new HashMap<>();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            String result = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closePay(String out_trade_no) {
        //1.封装参数
        Map param = new HashMap();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("out_trade_no", out_trade_no);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //2.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("调动查询API返回结果：" + xmlResult);

            return map;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
