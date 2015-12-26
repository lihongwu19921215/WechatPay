package com.zghm.wldm.third.wechat.controller;

import com.zghm.wldm.third.wechat.utils.GetWxOrderno;
import com.zghm.wldm.third.wechat.utils.RequestHandler;
import com.zghm.wldm.third.wechat.utils.TenpayUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.zghm.wldm.third.constant.PayConstant.DOMAIN_TEST;
/**
 * wldm
 * 微信扫码支付-模式二
 * @author Homiss
 * @version 1.0, 2015/12/16
 */
@Controller
public class WechatQrCodePay {

    private static final Logger logger = Logger.getLogger(WechatQrCodePay.class);

    @RequestMapping(value = "/config/weixinPay_notify")
    public static ModelAndView wechatQrcodePay(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {

        // 账号信息
        String appid = "";  // appid
        String appsecret = ""; // appsecret
        String mch_id = ""; // 商业号
        String key = ""; // key

        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;
        String order_price = "1"; // 价格
        String body = "Homiss";   // 商品名称
        String out_trade_no = "9527"; // 订单号

        // 获取发起电脑 ip
        String spbill_create_ip = request.getRemoteAddr();
        // 回调接口
        String notify_url = DOMAIN_TEST + "/config/weixinPay_result";

        String trade_type = "NATIVE";

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", appid);
        packageParams.put("mch_id", mch_id);
        packageParams.put("nonce_str", nonce_str);

        packageParams.put("body", body);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", order_price);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);

        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(appid, appsecret, key);

        String sign = requestHandler.createSign(packageParams);
        String xml="<xml>"+
                "<appid>"+appid+"</appid>"+
                "<mch_id>"+mch_id+"</mch_id>"+
                "<nonce_str>"+nonce_str+"</nonce_str>"+
                "<sign>"+sign+"</sign>"+
                "<body><![CDATA["+body+"]]></body>"+
                "<out_trade_no>"+out_trade_no+"</out_trade_no>"+
                "<total_fee>"+order_price+"</total_fee>"+
                "<spbill_create_ip>"+spbill_create_ip+"</spbill_create_ip>"+
                "<notify_url>"+notify_url+"</notify_url>"+
                "<trade_type>"+trade_type+"</trade_type>"+
                "</xml>";

        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String code_url ;
        code_url = new GetWxOrderno().getUrlCode(createOrderURL, xml);
        if(code_url.equals("")){
            logger.debug("code_url error");
        }
        ModelAndView model = new ModelAndView("/wldm/common/pay/wechat");
        model.addObject("type", attach);
        model.addObject("waterId", waterId);
        model.addObject("orderNum", out_trade_no);
        model.addObject("price", order_price);
        model.addObject("code_url", code_url);
        return model;
    }

}
