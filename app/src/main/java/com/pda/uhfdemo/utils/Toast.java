package com.pda.uhfdemo.utils;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.gg.reader.api.utils.StringUtils;

public class Toast {

    public static void show(String msg){
        SmartToast.showInCenter(StringUtils.isNullOfEmpty(msg)?"timeout":msg);
    }

}
