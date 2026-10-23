package com.pda.uhfdemo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.coder.zzq.smartshow.dialog.EnsureDialog;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.MsgAppGetSerialParam;
import com.gg.reader.api.protocol.gx.MsgAppRestoreDefault;
import com.gg.reader.api.protocol.gx.MsgAppSetSerialParam;
import com.gg.reader.api.protocol.gx.MsgBaseGetAutoDormancy;
import com.gg.reader.api.protocol.gx.MsgBaseGetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseGetTagLog;
import com.gg.reader.api.protocol.gx.MsgBaseSetAutoDormancy;
import com.gg.reader.api.protocol.gx.MsgBaseSetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseSetTagLog;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.MsgTestCarrierWave;
import com.gg.reader.api.protocol.gx.MsgTestVSWRcheck;
import com.gg.reader.api.utils.FrequencyUtils;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.LocalManageUtil;
import com.pda.uhfdemo.utils.Toast;
import com.tencent.mmkv.MMKV;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OtherActivity extends AppCompatActivity {


    @BindView(R.id.set_auto_mode)
    Spinner set_auto_mode;
    @BindView(R.id.set_auto_freeTime)
    EditText set_auto_freeTime;

    @BindView(R.id.filterTime)
    EditText filterTime;
    @BindView(R.id.rssiThreshold)
    EditText rssiThreshold;

    @BindView(R.id.set_frequency_spinner)
    Spinner set_frequency_spinner;

    @BindView(R.id.set_baudRate_spinner)
    Spinner set_baudRate_spinner;

    GClient client = GlobalClient.getClient();

    MMKV mmkv = MMKV.defaultMMKV();


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
//        LocalManageUtil.saveSelectLanguage(this, 0);
        ButterKnife.bind(this);
        initQuery();
    }


    public void initQuery() {
        MsgBaseStop stop = new MsgBaseStop();
        client.sendSynMsg(stop, 1500);
        if (stop.getRtCode() == 0) {
            autoQueryEvent();
            frequencyQueryEvent();
            uploadQueryEvent();
            baudRateQueryEvent();
        }
    }


    @OnClick(R.id.auto_query_btn)
    public void autoQueryEvent() {
        MsgBaseGetAutoDormancy msg = new MsgBaseGetAutoDormancy();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            set_auto_mode.setSelection(msg.getOnOff());
            set_auto_freeTime.setText(msg.getFreeTime() + "");
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.auto_set_btn)
    public void autoSetEvent() {
        String freeTime = set_auto_freeTime.getText().toString();
        if (StringUtils.isNullOfEmpty(freeTime)) {
            Toast.show(getString(R.string.setting_hint_1));
            return;
        }
        MsgBaseSetAutoDormancy msg = new MsgBaseSetAutoDormancy();
        msg.setOnOff(set_auto_mode.getSelectedItemPosition());
        msg.setFreeTime(Integer.parseInt(freeTime));
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.frequency_query_btn)
    public void frequencyQueryEvent() {
        MsgBaseGetFreqRange msg = new MsgBaseGetFreqRange();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            set_frequency_spinner.setSelection(msg.getFreqRangeIndex());
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.frequency_set_btn)
    public void frequencySetEvent() {
        MsgBaseSetFreqRange msg = new MsgBaseSetFreqRange();
        msg.setFreqRangeIndex(set_frequency_spinner.getSelectedItemPosition());
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());

    }

    @OnClick(R.id.upload_query_btn)
    public void uploadQueryEvent() {
        MsgBaseGetTagLog msg = new MsgBaseGetTagLog();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            filterTime.setText(msg.getRepeatedTime() + "");
            rssiThreshold.setText(msg.getRssiTV() + "");
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.upload_set_btn)
    public void uploadSetEvent() {
        MsgBaseSetTagLog msg = new MsgBaseSetTagLog();
        msg.setRepeatedTime(Integer.parseInt(filterTime.getText().toString()));
        msg.setRssiTV(Integer.parseInt(rssiThreshold.getText().toString()));
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }



    @OnClick(R.id.reset_btn)
    public void resetEvent() {
        new EnsureDialog()
                .title(getString(R.string.other_hint1))
                .message(getString(R.string.other_hint2))
                .confirmBtn("Ok", (dialog, which, data) -> {
                    MsgAppRestoreDefault msg = new MsgAppRestoreDefault();
                    client.sendSynMsg(msg);
                    Toast.show(msg.getRtMsg());
                    dialog.dismiss();
                }).cancelBtn("Cancel", (dialog, which, data) -> {
                    dialog.dismiss();
                }).showInActivity(this);
    }


    @OnClick(R.id.baudRate_query_btn)
    public void baudRateQueryEvent() {
        MsgAppGetSerialParam msg = new MsgAppGetSerialParam();
        client.sendSynMsg(msg);
        if (msg.getRtCode()==0){
            set_baudRate_spinner.setSelection(msg.getSerialBaudrate());
        } else {
            Toast.show(msg.getRtMsg());
        }
    }


    @OnClick(R.id.baudRate_set_btn)
    public void baudRateSetEvent () {
        MsgAppSetSerialParam msg = new MsgAppSetSerialParam();
        msg.setSerialBaudrate(set_baudRate_spinner.getSelectedItemPosition());
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            mmkv.encode("baudRate", set_baudRate_spinner.getSelectedItem().toString());
            
        } else {
            Toast.show(msg.getRtMsg());
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.sendUnsynMsg(new MsgBaseStop());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase));
    }

}