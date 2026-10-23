package com.pda.uhfdemo.init;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.coder.zzq.smartshow.core.SmartShow;
import com.coder.zzq.smartshow.toast.SmartToast;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.LocalManageUtil;
import com.pda.uhfdemo.utils.PowerUtil;
import com.tencent.mmkv.MMKV;

public class AppInit extends Application {

    GClient client = GlobalClient.getClient();

    @Override
    public void onCreate() {
        super.onCreate();
        String rootDir = MMKV.initialize(this);
        Log.e("mmkv root:", rootDir);
        SmartShow.init(this);
        LocalManageUtil.setApplicationLanguage(this);
        AppStateTracker.track(this, new AppStateTracker.AppStateChangeListener() {
            @Override
            public void appTurnIntoForeground() {
           
                PowerUtil.power("1");
                init();
            }

            @Override
            public void appTurnIntoBackGround() {
                
                PowerUtil.power("0");
                client.close();
            }
        });
    }

    private void init() {
        int[] baudRateList = {460800, 115200};
        for (int i : baudRateList) {
            boolean b = openReader(i + "");
            if (b) {
                break;
            }
        }
    }


    private boolean openReader(String baudRate) {
        if (client.openAndroidSerial("/dev/ttyS3:" + baudRate, 0)) {
            MsgBaseStop stop = new MsgBaseStop();
            client.sendSynMsg(stop);
            if (stop.getRtCode() == 0) {
                return stop.getRtCode() == 0;
            } else {
                client.close();
            }
        } else {
            SmartToast.showInCenter(R.string.toast_open_serial);
        }
        return false;
    }


    @Override
    protected void attachBaseContext(Context base) {
        LocalManageUtil.saveSystemCurrentLanguage(base);
        super.attachBaseContext(LocalManageUtil.setLocal(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocalManageUtil.onConfigurationChanged(getApplicationContext());
    }
}
