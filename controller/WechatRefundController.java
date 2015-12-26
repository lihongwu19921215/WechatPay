package com.zghm.wldm.third.wechat.controller;

import com.zghm.wldm.entity.ResultEntity;
import com.zghm.wldm.third.wechat.utils.GetWxOrderno;
import com.zghm.wldm.third.wechat.utils.RequestHandler;
import com.zghm.wldm.third.wechat.utils.TenpayUtil;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * wldm
 * 微信退款
 *
 * @author Homiss
 * @version 1.0, 2015/12/21
 */
public class WechatRefundController {

    private static final Logger logger = Logger.getLogger(WechatRefundController.class);

    public static ResultEntity wechatRefund(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {
        // 返回信息
        ResultEntity result = new ResultEntity();

        // 账号信息
        String appid = "";  // appid
        String appsecret = ""; // appsecret
        String mch_id = ""; // 商业号
        String key = ""; // key

        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        SortedMap<String, String> parameters = new TreeMap<String, String>();
        parameters.put("appid", appid);
        parameters.put("mch_id", mch_id);
        parameters.put("nonce_str", nonce_str);
        // 在notify_url中解析微信返回的信息获取到 transaction_id，此项不是必填，详细请看上图文档
        // parameters.put("transaction_id", "微信支付订单中调用统一接口后微信返回的 transaction_id");
        parameters.put("out_trade_no", "9527");
        parameters.put("out_refund_no", "9527");                              //我们自己设定的退款申请号，约束为UK
        parameters.put("total_fee", "1");          //单位为分
        parameters.put("refund_fee", "1");              //单位为分
        // 操作员帐号, 默认为商户号
        parameters.put("op_user_id", mch_id);
        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(appid, appsecret, key);
        String sign = requestHandler.createSign(parameters);

        String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

        String xml = "<xml>"
                + "<appid><![CDATA[" + appid + "]]></appid>"
                + "<mch_id><![CDATA[" + mch_id +"]]></mch_id>"
                + "<nonce_str><![CDATA[" + nonce_str + "]]></nonce_str>"
                + "<out_trade_no><![CDATA[" + out_trade_no + "]]></out_trade_no>"
                + "<out_refund_no><![CDATA[" + out_refund_no + "]]></out_refund_no>"
                + "<total_fee><![CDATA[" + total_fee + "]]></total_fee>"
                + "<refund_fee><![CDATA[" + refund_fee + "]]></refund_fee>"
                + "<op_user_id><![CDATA[" + mch_id + "]]></op_user_id>"
                + "<sign>" + sign + "</sign>"
                + "</xml>";

        try {
            Map map = GetWxOrderno.forRefund(createOrderURL, xml);
            if(map != null){
                String return_code = (String) map.get("return_code");
                String result_code = (String) map.get("result_code");
                if(return_code.equals("SUCCESS") && result_code.equals("SUCCESS")){
                    // 退款成功
                    result.setMsg("退款成功");
                    result.setFlag(1);
                } else {
                    result.setMsg("退款失败");
                }
            } else {
                result.setMsg("退款失败");
            }
        } catch (Exception e) {
            System.out.print("退款失败");
            e.printStackTrace();
        }
        return result;
    }
}
