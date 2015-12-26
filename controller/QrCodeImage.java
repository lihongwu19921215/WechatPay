package com.zghm.wldm.third.wechat.controller;

import com.zghm.wldm.util.QRCodeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * wldm
 *
 * @author Homiss
 * @version 1.0, 2015/12/18
 */
@Controller
public class QrCodeImage {

    @RequestMapping("/qr_code.img")
    @ResponseBody
    public void getQrCode(String code_url, HttpServletResponse response) throws Exception {
        // String qrCode = QRCodeUtil.encode("qrCode", qrCodePath);
        ServletOutputStream sos = response.getOutputStream();
        QRCodeUtil.encode(code_url, sos);
    }
}
