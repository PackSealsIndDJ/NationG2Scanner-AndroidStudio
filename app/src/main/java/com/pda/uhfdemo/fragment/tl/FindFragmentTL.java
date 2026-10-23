package com.pda.uhfdemo.fragment.tl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.dal.HandlerTagTLLog;
import com.gg.reader.api.dal.HandlerTagTLOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.LogBaseTLInfo;
import com.gg.reader.api.protocol.gx.LogBaseTLOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryTL;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FindFragmentTL extends Fragment {


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
        View view = inflater.inflate(R.layout.fragment_find_tl, container, false);
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
        client.onTagTLog = new HandlerTagTLLog() {
            @Override
            public void log(String s, LogBaseTLInfo logBaseTLInfo) {
                    String epc = logBaseTLInfo.getEpc();
                    if (!findMap.containsKey(epc)) {
                        if (mmkv.containsKey(epc)) {
                            VoiceUtils.start();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findMap.put(epc, new BindInfo(epc, mmkv.decodeString(epc)));
                                    findInfoList.clear();
                                    findInfoList.addAll(findMap.values());
                                    findAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

            }
        };
        client.onTagTLOver = new HandlerTagTLOver() {
            @Override
            public void log(String s, LogBaseTLOver logBaseTLOver) {

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
            MsgBaseInventoryTL msg = new MsgBaseInventoryTL();
            msg.setAntennaEnable(EnumG.AntennaNo_1);
            msg.setInventoryMode(EnumG.InventoryMode_Inventory);
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