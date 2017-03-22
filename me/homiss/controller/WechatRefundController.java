package me.homiss.controller;

import me.homiss.constant.GlobalConfig;
import me.homiss.utils.GetWxOrderno;
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
 * 微信退款
 * @author Homiss
 * @version 1.0, 2015/12/21
 */
@Controller
@RequestMapping("/")
public class WechatRefundController {

    @RequestMapping("/wechat/pay/refund")
    @ResponseBody
    public static String wechatRefund(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {

        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        String out_trade_no = "";
        String out_refund_no = "";
        String total_fee = "";
        String refund_fee = "";

        SortedMap<String, String> parameters = new TreeMap<String, String>();
        parameters.put("appid", GlobalConfig.APPID);
        parameters.put("mch_id", GlobalConfig.MCH_ID);
        parameters.put("nonce_str", nonce_str);
        // 在notify_url中解析微信返回的信息获取到 transaction_id，此项不是必填，详细请看上图文档
        // parameters.put("transaction_id", "微信支付订单中调用统一接口后微信返回的 transaction_id");
        parameters.put("out_trade_no", out_trade_no);
        parameters.put("out_refund_no", out_refund_no); //我们自己设定的退款申请号，约束为UK
        parameters.put("total_fee", total_fee); //单位为分
        parameters.put("refund_fee", refund_fee); //单位为分
        // 操作员帐号, 默认为商户号
        parameters.put("op_user_id", GlobalConfig.MCH_ID);
        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(GlobalConfig.APPID, GlobalConfig.APPSECRET, GlobalConfig.KEY);
        String sign = requestHandler.createSign(parameters);

        String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

        String xml = "<xml>"
                + "<appid><![CDATA[" + GlobalConfig.APPID + "]]></appid>"
                + "<mch_id><![CDATA[" + GlobalConfig.MCH_ID +"]]></mch_id>"
                + "<nonce_str><![CDATA[" + nonce_str + "]]></nonce_str>"
                + "<out_trade_no><![CDATA[" + out_trade_no + "]]></out_trade_no>"
                + "<out_refund_no><![CDATA[" + out_refund_no + "]]></out_refund_no>"
                + "<total_fee><![CDATA[" + total_fee + "]]></total_fee>"
                + "<refund_fee><![CDATA[" + refund_fee + "]]></refund_fee>"
                + "<op_user_id><![CDATA[" + GlobalConfig.MCH_ID + "]]></op_user_id>"
                + "<sign>" + sign + "</sign>"
                + "</xml>";

        try {
            Map map = GetWxOrderno.forRefund(createOrderURL, xml);
            if(map != null){
                String return_code = (String) map.get("return_code");
                String result_code = (String) map.get("result_code");
                if(return_code.equals("SUCCESS") && result_code.equals("SUCCESS")){
                    System.out.println("退款成功");
                } else {
                    System.out.println("退款失败");
                }
            } else {
                System.out.println("退款失败");
            }
        } catch (Exception e) {
            System.out.print("退款失败");
            e.printStackTrace();
        }
        return null;
    }
}
