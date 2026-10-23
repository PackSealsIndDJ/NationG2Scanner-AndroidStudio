package com.pda.uhfdemo;

import static com.coder.zzq.toolkit.Toolkit.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.MsgBaseGetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseGetFrequency;
import com.gg.reader.api.protocol.gx.MsgBaseSetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseSetFrequency;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.utils.FrequencyUtils;
import com.pda.uhfdemo.adapter.FrequencyAdapter;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FrequencyActivity extends AppCompatActivity {

    @BindView(R.id.set_frequency_spinner)
    Spinner set_frequency_spinner;
    @BindView(R.id.autoFreq_mode)
    Spinner autoFreq_mode;
    @BindView(R.id.autoFreq_recyclerview)
    RecyclerView autoFreq_recyclerview;
    @BindView(R.id.autoFreq_query_btn)
    Button autoFreq_query_btn;
    @BindView(R.id.autoFreq_set_btn)
    Button autoFreq_set_btn;

    GClient client = GlobalClient.getClient();
    FrequencyAdapter frequencyAdapter;

    List<PointInfo> frequencyList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);
        ButterKnife.bind(this);
        initAdapter();
        MsgBaseStop stop = new MsgBaseStop();
        client.sendSynMsg(stop, 1500);
        if (stop.getRtCode() == 0) {
            frequencyQueryEvent();
            autoFrequencyQueryEvent();
        }
    }


    private void initAdapter() {
        frequencyAdapter = new FrequencyAdapter(frequencyList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        autoFreq_recyclerview.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        autoFreq_recyclerview.setLayoutManager(layoutManager);
        autoFreq_recyclerview.setAdapter(frequencyAdapter);
    }

    @OnClick(R.id.frequency_query_btn)
    public void frequencyQueryEvent() {
        MsgBaseGetFreqRange msg = new MsgBaseGetFreqRange();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            set_frequency_spinner.setSelection(msg.getFreqRangeIndex());
            frequencyList.clear();
            List<String> pointList = FrequencyUtils.indexGetFre(msg.getFreqRangeIndex());
            for (String point : pointList) {
                frequencyList.add(new PointInfo(false, point));
            }
            frequencyAdapter.notifyDataSetChanged();
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
            frequencyList.clear();
            List<String> pointList = FrequencyUtils.indexGetFre(msg.getFreqRangeIndex());
            for (String point : pointList) {
                frequencyList.add(new PointInfo(false, point));
            }
            frequencyAdapter.notifyDataSetChanged();
        }
        Toast.show(msg.getRtMsg());
    }


    @OnClick(R.id.autoFreq_query_btn)
    public void autoFrequencyQueryEvent() {
        MsgBaseGetFrequency msg = new MsgBaseGetFrequency();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            autoFreq_mode.setSelection(msg.getAutomatically() ? 1 : 0);
            for (PointInfo info : frequencyList) {
                info.setChecked(false);
            }
            for (int index : msg.getListFreqCursor()) {
                frequencyList.get(index).setChecked(true);
            }
            frequencyAdapter.notifyDataSetChanged();
        } else {
            Toast.show(msg.getRtMsg());
        }
    }


    @OnClick(R.id.autoFreq_set_btn)
    public void autoFrequencySetEvent() {
        MsgBaseSetFrequency msg = new MsgBaseSetFrequency();
        msg.setAutomatically(autoFreq_mode.getSelectedItemPosition() == 1);
        if (!msg.getAutomatically()) {
            ArrayList<Integer> indexList = new ArrayList<>();
            for (int i = 0; i < frequencyList.size(); i++) {
                if (frequencyList.get(i).isChecked()) {
                    indexList.add(i);
                }
            }
            msg.setListFreqCursor(indexList);
        }
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    public static class PointInfo {
        private boolean checked;
        private String text;

        public PointInfo(boolean checked, String text) {
            this.checked = checked;
            this.text = text;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}