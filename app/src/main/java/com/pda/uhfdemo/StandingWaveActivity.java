package com.pda.uhfdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.MsgBaseGetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseSetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.MsgTestCarrierWave;
import com.gg.reader.api.protocol.gx.MsgTestVSWRcheck;
import com.gg.reader.api.utils.FrequencyUtils;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StandingWaveActivity extends AppCompatActivity {

    @BindView(R.id.set_frequency_spinner)
    Spinner set_frequency_spinner;
    @BindView(R.id.point_spinner)
    Spinner point_spinner;
    @BindView(R.id.prePower)
    EditText prePower;
    @BindView(R.id.sufPower)
    EditText sufPower;

    GClient client = GlobalClient.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standing_wave);
        ButterKnife.bind(this);
        initQuery();
    }


    public void initQuery() {
        MsgBaseStop stop = new MsgBaseStop();
        client.sendSynMsg(stop, 1500);
        if (stop.getRtCode() == 0) {
            frequencyQueryEvent();
        }
    }


    @OnClick(R.id.frequency_query_btn)
    public void frequencyQueryEvent() {
        MsgBaseGetFreqRange msg = new MsgBaseGetFreqRange();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            set_frequency_spinner.setSelection(msg.getFreqRangeIndex());
            ArrayAdapter<String> freAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, FrequencyUtils.indexGetFre(msg.getFreqRangeIndex()));
            point_spinner.setAdapter(freAdapter);
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.frequency_set_btn)
    public void frequencySetEvent() {
        MsgBaseSetFreqRange msg = new MsgBaseSetFreqRange();
        msg.setFreqRangeIndex(set_frequency_spinner.getSelectedItemPosition());
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            ArrayAdapter<String> freAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, FrequencyUtils.indexGetFre(msg.getFreqRangeIndex()));
            point_spinner.setAdapter(freAdapter);
        }
        Toast.show(msg.getRtMsg());

    }

    @OnClick(R.id.carrier_btn)
    public void carrierEvent() {
        MsgTestCarrierWave msg = new MsgTestCarrierWave();
        msg.setAntennaNum(EnumG.AntennaNo_1);
        msg.setFreqCursor(point_spinner.getSelectedItemPosition());
        client.sendSynMsg(msg);
        if (msg.getRtCode() != 0) {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.stopCarrier_btn)
    public void stopCarrierEvent() {
        MsgBaseStop msg = new MsgBaseStop();
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.detection_btn)
    public void detectionEvent() {
        MsgTestVSWRcheck msg = new MsgTestVSWRcheck();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            prePower.setText(msg.getPreValue() + "");
            sufPower.setText(msg.getSufValue() + "");
        } else {
            Toast.show(msg.getRtMsg());
        }
    }


}