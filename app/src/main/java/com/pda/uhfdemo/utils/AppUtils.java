package com.pda.uhfdemo.utils;

import android.app.Activity;
import android.content.Intent;


public class AppUtils {

    public static void reset(Activity activity) {
        activity.finish();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Intent LaunchIntent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(LaunchIntent);
    }
}
