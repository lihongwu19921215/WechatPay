package me.homiss.controller;

import me.homiss.constant.GlobalConfig;
import me.homiss.utils.GetWxOrderno;
import me.homiss.utils.IdWorker;
import me.homiss.utils.RequestHandler;
import me.homiss.utils.TenpayUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信扫码支付-模式二
 * @author Homiss
 * @version 1.0, 2015/12/16
 */
@Controller
@RequestMapping("/")
public class WechatQrCodePayController {

    /**
     * 生成预付订单，并返回前端需要的参数
     * @param response
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/wechat/pay/preorder")
    @ResponseBody
    public static Map<String, String> wechatQrcodePay(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {

        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;
        String order_price = "1"; // 价格 这里的单位是 分
        String body = "Homiss";   // 商品名称
        String out_trade_no = String.valueOf(new IdWorker(1).nextId()); // 订单号 随机生成

        // 获取发起电脑 ip
        String spbill_create_ip = request.getRemoteAddr();
        // 回调接口
        String notify_url = "这里必须填写微信能够访问到的域名，可以使用 ngrok 这个软件" + "/wechat/pay/callback";
        // 根据支付类型不同需要修改type的值
        String trade_type = "NATIVE";

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", GlobalConfig.APPID);
        packageParams.put("mch_id", GlobalConfig.MCH_ID);
        packageParams.put("nonce_str", nonce_str);

        packageParams.put("body", body);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", order_price);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);

        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(GlobalConfig.APPID, GlobalConfig.APPSECRET, GlobalConfig.KEY);

        String sign = requestHandler.createSign(packageParams);
        String xml="<xml>"+
                "<appid>" + GlobalConfig.APPID + "</appid>"+
                "<mch_id>" + GlobalConfig.MCH_ID + "</mch_id>"+
                "<nonce_str>" + nonce_str + "</nonce_str>"+
                "<sign>" + sign + "</sign>"+
                "<body><![CDATA[" + body + "]]></body>"+
                "<out_trade_no>" + out_trade_no + "</out_trade_no>"+
                "<total_fee>" + order_price + "</total_fee>"+
                "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"+
                "<notify_url>" + notify_url + "</notify_url>" +
                "<trade_type>" + trade_type + "</trade_type>"+
                "</xml>";

        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        Map<String, String> params = new GetWxOrderno().getPreOrder(createOrderURL, xml);

        System.out.println("================== 打印统一预付订单返回的值 ==================");
        for(String key : params.keySet()){
            System.out.println(key + " -> " + params.get(key));
        }

        return params;
    }

}
