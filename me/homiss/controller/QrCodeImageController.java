package me.homiss.controller;

import me.homiss.utils.http.QRCodeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;


/**
 * 通过codeUrl的值生成二维码并返回
 * @author Homiss
 * @version 1.0, 2015/12/18
 */
@Controller
public class QrCodeImageController {

    @RequestMapping("/qrcode.img")
    @ResponseBody
    public void getQrCode(String codeUrl, HttpServletResponse response) throws Exception {
        // String qrCode = QRCodeUtil.encode("qrCode", qrCodePath);
        ServletOutputStream sos = response.getOutputStream();
        QRCodeUtil.encode(codeUrl, sos);
    }


}
