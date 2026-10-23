package com.pda.uhfdemo.fragment.sixb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTag6bLog;
import com.gg.reader.api.dal.HandlerTag6bOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBase6bInfo;
import com.gg.reader.api.protocol.gx.LogBase6bOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventory6b;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.adapter.FindAdapter;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.Toast;
import com.pda.uhfdemo.utils.VoiceUtils;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FindFragment6b extends Fragment {


    @BindView(R.id.find_listview)
    RecyclerView findListView;
    @BindView(R.id.find_btn)
    Button findBtn;
    private FindAdapter findAdapter;

    List<BindInfo> findInfoList = new ArrayList<>();
    LinkedHashMap<String, BindInfo> findMap = new LinkedHashMap<>();

    GClient client = GlobalClient.getClient();

    MMKV mmkv = MMKV.defaultMMKV();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_6b, container, false);
        ButterKnife.bind(this, view);
        initAdapter();
        return view;
    }

    private void initAdapter() {
        findAdapter = new FindAdapter(findInfoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        findListView.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        findListView.setLayoutManager(layoutManager);
        findListView.setAdapter(findAdapter);
    }

    public void tagCallback() {
        client.onTag6bLog = new HandlerTag6bLog() {
            @Override
            public void log(String s, LogBase6bInfo logBase6bInfo) {
                if (logBase6bInfo.getResult() == 0) {
                    String tid = logBase6bInfo.getTid();
                    if (!findMap.containsKey(tid)) {
                        if (mmkv.containsKey(tid)) {
                            VoiceUtils.start();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findMap.put(tid, new BindInfo(tid, mmkv.decodeString(tid)));
                                    findInfoList.clear();
                                    findInfoList.addAll(findMap.values());
                                    findAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        };
        client.onTag6bOver = new HandlerTag6bOver() {
            @Override
            public void log(String s, LogBase6bOver logBase6bOver) {

            }
        };
    }


    @OnClick(R.id.find_btn)
    public void findEvent() {
        if (findBtn.getText().equals(getResources().getString(R.string.scan_btn))) {
            tagCallback();
            findInfoList.clear();
            findMap.clear();
            findAdapter.notifyDataSetChanged();
            client.sendUnsynMsg(new MsgBaseStop());
            MsgBaseInventory6b msg = new MsgBaseInventory6b();
            msg.setAntennaEnable(EnumG.AntennaNo_1);
            msg.setInventoryMode(EnumG.InventoryMode_Inventory);
            msg.setArea(EnumG.ReadMode6b_Tid);
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                findBtn.setText(getResources().getString(R.string.stop_btn));
            } else {
                Toast.show(getString(R.string.find_hint_1));
            }
        } else {
            MsgBaseStop msg = new MsgBaseStop();
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                findBtn.setText(getResources().getString(R.string.scan_btn));
            } else {
                Toast.show(getString(R.string.find_hint_2));
            }
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!findBtn.getText().equals(getResources().getString(R.string.scan_btn))) {
            client.sendUnsynMsg(new MsgBaseStop());
            findBtn.setText(getResources().getString(R.string.scan_btn));
        }
    }
}