package com.pda.uhfdemo.utils;

import android.util.Log;

import java.io.FileWriter;

public class PowerUtil {

//    private static String s1 = "/proc/gpiocontrol/set_id";
    private static String s2 = "/proc/gpiocontrol/set_uhf";
    private static String s3 = "/proc/gpiocontrol/set_bd";

    public static void power(String state) {
        try {

            FileWriter localFileWriterOn1 = new FileWriter(s2);
            localFileWriterOn1.write(state);
            localFileWriterOn1.close();
            Log.e("PowerUtil", "power=" + state + " Path=" + s2);
            Thread.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
