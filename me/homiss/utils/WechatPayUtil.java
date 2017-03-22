package me.homiss.utils;

import cn.zhikr.wechatshare.pay.constant.GlobalConfig;
import cn.zhikr.wechatshare.pay.pojo.ProjectOrder;
import cn.zhikr.wechatshare.pay.utils.client.ResponseHandler;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created with wechatShare.
 * User : Homiss
 * Date : 2017/3/21
 * Time : 17:01
 */
public class WechatPayUtil {

    private final static Logger logger = LoggerFactory.getLogger(WechatPayUtil.class);

    public static Map<String, String> appPay(HttpServletResponse response, HttpServletRequest request,
                                             ProjectOrder projectOrder){
        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        String orderPrice = String.valueOf(projectOrder.getTotalFee() * 100); // 微信单位为分
        String body = projectOrder.getProjectName();   // 商品名称
        String out_trade_no = projectOrder.getOrderNo(); // 订单号

        // 获取发起电脑 ip
        String spbill_create_ip = request.getRemoteAddr();
        // 回调接口
        String notify_url = "http://ccb21414.ngrok.io/app/pay/wechat/callback";

        String trade_type = "APP";

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", GlobalConfig.APPID);
        packageParams.put("mch_id", GlobalConfig.MCH_ID);
        packageParams.put("nonce_str", nonce_str);

        packageParams.put("body", body);
        packageParams.put("out_trade_no", out_trade_no);
        packageParams.put("total_fee", orderPrice);
        packageParams.put("spbill_create_ip", spbill_create_ip);
        packageParams.put("notify_url", notify_url);
        packageParams.put("trade_type", trade_type);

        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(GlobalConfig.APPID, GlobalConfig.APPSECRET, GlobalConfig.KEY);

        String sign = requestHandler.createSign(packageParams);
        String xml="<xml>"+
                "<appid>"+ GlobalConfig.APPID + "</appid>" +
                "<mch_id>"+ GlobalConfig.MCH_ID + "</mch_id>" +
                "<nonce_str>" + nonce_str + "</nonce_str>" +
                "<sign>" + sign + "</sign>" +
                "<body><![CDATA[" + body + "]]></body>" +
                "<out_trade_no>" + out_trade_no + "</out_trade_no>" +
                "<total_fee>" + orderPrice + "</total_fee>" +
                "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>" +
                "<notify_url>" + notify_url + "</notify_url>" +
                "<trade_type>" + trade_type + "</trade_type>" +
                "</xml>";

        String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        Map<String, String> result = new GetWxOrderno().getUrlCode(createOrderURL, xml);
        result.remove("appid");
        result.remove("mch_id");
        result.put("timestamp", String.valueOf(System.currentTimeMillis()).substring(0, 10));
        // 为移动端调用生成sign值
        sign = getSign(response, request, result);
        result.put("sign", sign);
        return result;
    }

    /**
     * 根据param生成sign
     * @param response
     * @param request
     * @param param
     * @return
     */
    private static String getSign(HttpServletResponse response, HttpServletRequest request,
                           Map<String, String> param){
        String appid = GlobalConfig.APPID;  // appid
        String appsecret = GlobalConfig.APPSECRET; // appsecret
        String key = GlobalConfig.KEY; // key
        String mch_id = GlobalConfig.MCH_ID;

        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", appid);
        packageParams.put("prepayid", param.get("prepay_id"));
        packageParams.put("partnerid", mch_id);
        packageParams.put("package", "Sign=WXPay");
        packageParams.put("noncestr", param.get("nonce_str"));
        packageParams.put("timestamp", param.get("timestamp"));

        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(appid, appsecret, key);

        String sign = requestHandler.createSign(packageParams);
        return sign;
    }

    public static Map<String, String> orderBack(HttpServletRequest request, HttpServletResponse response) throws IOException, JDOMException {
        Map<String, String> result = null;
        //创建支付应答对象
        ResponseHandler resHandler = new ResponseHandler(request, response);
        resHandler.setKey(GlobalConfig.KEY);
        //判断签名是否正确
        if(resHandler.isTenpaySign()) {
            //处理业务开始
            String resXml;
            if("SUCCESS".equals(resHandler.getParameter("result_code"))){
                // 同步返回给微信参数
                //通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                resHandler.getAllParameters();
                String transactionId = resHandler.getParameter("transaction_id");
                String outTradeNo = resHandler.getParameter("out_trade_no");
                String attach = resHandler.getParameter("attach");
                String totalFee = resHandler.getParameter("total_fee");
                System.out.println("======================");
                System.out.println(transactionId);
                System.out.println(outTradeNo);
                System.out.println(attach);
                System.out.println(totalFee);
                System.out.println("======================");
                result = new HashMap<>();
                result.put("transactionId", transactionId);
                result.put("outTradeNo", outTradeNo);
                result.put("attach", attach);
                result.put("totalFee", totalFee);
            } else {
                System.out.println("支付失败,错误信息：" + resHandler.getParameter("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
                        + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            //------------------------------
            //处理业务完毕
            //------------------------------
            BufferedOutputStream out = new BufferedOutputStream(
                    response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } else{
            System.out.println("通知签名验证失败");
        }
        return result;
    }
}
