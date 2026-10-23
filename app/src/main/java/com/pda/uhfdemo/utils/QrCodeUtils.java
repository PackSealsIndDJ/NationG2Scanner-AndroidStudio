package com.pda.uhfdemo.utils;

import android.graphics.Bitmap;


import com.uuzuche.lib_zxing.activity.CodeUtils;

public class QrCodeUtils {

    public static Bitmap getQrCode(String code){
        return CodeUtils.createImage(code,350,350,null);
    }

}
