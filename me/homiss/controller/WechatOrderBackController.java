package me.homiss.controller;


import me.homiss.constant.GlobalConfig;
import me.homiss.utils.client.ResponseHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;

/**
 * 微信回调接口
 * @author Homiss
 * @version 1.0, 2015/12/16
 */
@Controller
@RequestMapping("/")
public class WechatOrderBackController {

    @RequestMapping(value = "/wechat/pay/callback")
    public void wechatOrderBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //---------------------------------------------------------
        //财付通支付通知（后台通知）
        //---------------------------------------------------------
        //创建支付应答对象
        ResponseHandler resHandler = new ResponseHandler(request, response);
        resHandler.setKey(GlobalConfig.KEY);
        //判断签名是否正确
        if(resHandler.isTenpaySign()) {
            //------------------------------
            //处理业务开始
            //------------------------------
            String resXml = "";
            if("SUCCESS".equals(resHandler.getParameter("result_code"))){
                // 同步返回给微信参数
				// resHandler.sendToCFT("SUCCESS");
				//通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
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
    }


}
