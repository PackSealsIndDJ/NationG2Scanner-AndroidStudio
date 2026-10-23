package com.pda.uhfdemo.fragment.sixb;

import com.pda.uhfdemo.ScanBeep;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coder.zzq.smartshow.dialog.ChooseListDialog;
import com.coder.zzq.smartshow.dialog.ChooseResult;
import com.coder.zzq.smartshow.toast.SmartToast;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerDebugLog;
import com.gg.reader.api.dal.HandlerTag6bLog;
import com.gg.reader.api.dal.HandlerTag6bOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBase6bInfo;
import com.gg.reader.api.protocol.gx.LogBase6bOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventory6b;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.Param6bReadUserdata;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.FrequencyActivity;
import com.pda.uhfdemo.OtherActivity;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.StandingWaveActivity;
import com.pda.uhfdemo.UpgradeActivity;
import com.pda.uhfdemo.adapter.TagAdapter;
import com.pda.uhfdemo.entity.TagInfo;
import com.pda.uhfdemo.utils.ExcelUtil;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.Toast;
import com.pda.uhfdemo.utils.VoiceUtils;
import com.tencent.mmkv.MMKV;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ReadFragment6b extends Fragment {

    @BindView(R.id.inventory_btn)
    Button inventory_btn;
    @BindView(R.id.read_listView)
    RecyclerView listView;
    @BindView(R.id.tagTotal)
    TextView tagTotal;
    @BindView(R.id.tagSpeed)
    TextView tagSpeed;
    @BindView(R.id.tagReadTime)
    TextView tagReadTime;

    @BindView(R.id.match_read_content)
    EditText match_read_content;


    @BindView(R.id.title_tid)
    TextView title_tid;
    @BindView(R.id.title_user)
    TextView title_user;
    @BindView(R.id.title_reserved)
    TextView title_reserved;

    GClient client = GlobalClient.getClient();
    List<TagInfo> tagList = new ArrayList<>();

    final LinkedHashMap<String, TagInfo> tagMap = new LinkedHashMap<>();

    TagAdapter tagAdapter;
    View clickView;

    MMKV mmkv = MMKV.defaultMMKV();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory_6b, container, false);
        ButterKnife.bind(this, view);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        VoiceUtils.init(getContext());
        initAdapter();
        Log.e("ReadFragment", "ReadFragment");
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMenu();
    }

    private void initMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuProvider.super.onPrepareMenu(menu);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.cus_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_export:
                        String filePath = Environment.getExternalStorageDirectory() + "/Download/";
                        String fileName = "Tag_" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".xls";
                        String[] title = {"Index", "Type", "EPC", "TID", "UserData", "Reserved", "TotalCount", "ReadTime"};
                        if (tagList.size() > 0) {
                            try {
                                ExcelUtil.initExcel(filePath, fileName, title);
                                ExcelUtil.writeObjListToExcel(ExcelUtil.getRecordData(tagList), filePath + fileName);
                                SmartToast.show("Export success " + "Path=" + filePath + fileName);
                            } catch (Exception ex) {
                                SmartToast.show("Export Failed");
                                ex.printStackTrace();
                            }
                        } else {
                            SmartToast.show("No Data");
                        }
                        break;
                    case R.id.menu_other:
                        if (!inventory_btn.getText().equals(getString(R.string.read_btn))) {
                            Toast.show(getString(R.string.stop_inventory_hint));
                            return false;
                        }
                        startActivity(new Intent(getActivity(), OtherActivity.class));
                        break;
                    case R.id.menu_frequency:
                        if (!inventory_btn.getText().equals(getString(R.string.read_btn))) {
                            Toast.show(getString(R.string.stop_inventory_hint));
                            return false;
                        }
                        startActivity(new Intent(getActivity(), FrequencyActivity.class));
                        break;
                    case R.id.menu_upgrade:
                        if (!inventory_btn.getText().equals(getString(R.string.read_btn))) {
                            Toast.show(getString(R.string.stop_inventory_hint));
                            return false;
                        }
                        startActivity(new Intent(getActivity(), UpgradeActivity.class));
                        break;
                    case R.id.menu_standingWave:
                        if (!inventory_btn.getText().equals(getString(R.string.read_btn))) {
                            Toast.show(getString(R.string.stop_inventory_hint));
                            return false;
                        }
                        startActivity(new Intent(getActivity(), StandingWaveActivity.class));
                        break;
                }
                return false;
            }

            @Override
            public void onMenuClosed(@NonNull Menu menu) {
                MenuProvider.super.onMenuClosed(menu);
            }
        });
    }

    private void initAdapter() {
        tagAdapter = new TagAdapter(tagList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(tagAdapter);
        tagAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (clickView != null && view != clickView) {
                clickView.setBackgroundColor(Color.WHITE);
            }
            clickView = view;
            view.setBackgroundColor(Color.rgb(135, 206, 235));
//            BindFragment6b bindFragment6b = (BindFragment6b) getActivity().getSupportFragmentManager().getFragments().get(1);
//            bindFragment6b.selectData(position);
//            WriteFragment6b writeFragment6b = (WriteFragment6b) getActivity().getSupportFragmentManager().getFragments().get(3);
//            writeFragment6b.selectData(position + 1);
        });
        tagAdapter.setOnItemLongClickListener((baseQuickAdapter, view, i) -> {
            TagInfo tagInfo = tagList.get(i);
//            Log.e("tagInfo", tagInfo.getEpc());
            ChooseListDialog chooseListDialog = new ChooseListDialog();
            chooseListDialog.choiceMode(ChooseListDialog.CHOICE_MODE_SINGLE);
            ArrayList<String> dataList = new ArrayList<>();
            if (!tagInfo.getEpc().isEmpty()) {
                dataList.add(/*"EPC=" + */tagInfo.getEpc());
            }

            if (!TextUtils.isEmpty(tagInfo.getTid())) {
                dataList.add(/*"TID=" + */tagInfo.getTid());
            }
            if (!TextUtils.isEmpty(tagInfo.getUserData())) {
                dataList.add(/*"User=" + */tagInfo.getUserData());
            }
            if (!TextUtils.isEmpty(tagInfo.getReserved())) {
                dataList.add(/*"Reserved=" + */tagInfo.getReserved());
            }
            chooseListDialog.items(dataList);
            if (!dataList.isEmpty()) {
                chooseListDialog.defaultChoosePos(0);
            }
            chooseListDialog.cancelBtn("Cancel");
            chooseListDialog.confirmBtn("Copy", (dialog, which, data) -> {
                ChooseResult chooseResult = (ChooseResult) data;
                ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", chooseResult.getChooseItems()[0].toString());
                manager.setPrimaryClip(mClipData);
                Toast.show(chooseResult.getChooseItems()[0].toString());
                dialog.dismiss();
            });
            chooseListDialog.showInFragment(this);
            return false;
        });
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    VoiceUtils.start();
                    tagList.clear();
                    synchronized (tagMap) {
                        tagList.addAll(tagMap.values());
                    }
                    tagAdapter.notifyDataSetChanged();
                    tagTotal.setText(tagMap.size() + "");
                    break;
                case 1:
//                    if (timer != null) {
//                        timer.cancel();
//                        timer = null;
//                    }
                    if (getActivity()!=null) {
                        BindFragment6b bindFragment6b = (BindFragment6b) getActivity().getSupportFragmentManager().getFragments().get(1);
                        bindFragment6b.setData(tagList);
                        WriteFragment6b writeFragment6b = (WriteFragment6b) getActivity().getSupportFragmentManager().getFragments().get(3);
                        writeFragment6b.setData(tagList);
                    }
                    break;
                case 2:
                    tagReadTime.setText(time + "ms");
                    break;
                case 3:
                    tagSpeed.setText(msg.arg1 + "t/s");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void onTagCallback() {
        client.onTag6bLog = new HandlerTag6bLog() {
            @Override
            public void log(String s, LogBase6bInfo logBase6bInfo) {
                if (logBase6bInfo.getResult() == 0) {
//                    Log.e("epc", logBaseEpcInfo.getEpc());
                    synchronized (tagMap) {
                        processTag(logBase6bInfo);
                    }

                }
            }
        };
        client.onTag6bOver = new HandlerTag6bOver() {
            @Override
            public void log(String s, LogBase6bOver logBase6bOver) {
                Log.e("HandlerTag6bOver", logBase6bOver.getRtMsg());
                handler.sendEmptyMessage(1);
            }
        };
        client.debugLog=new HandlerDebugLog() {
            @Override
            public void sendDebugLog(String s) {
                Log.e("log",s);
            }

            @Override
            public void receiveDebugLog(String s) {
                Log.e("log",s);
            }

            @Override
            public void crcErrorLog(String s) {

            }
        };
    }

    private void processTag(LogBase6bInfo logBase6bInfo) {
        ScanBeep.play();
        if (tagMap.containsKey(logBase6bInfo.getTid())) {
            TagInfo tagInfo = tagMap.get(logBase6bInfo.getTid());
            tagInfo.setCount(tagInfo.getCount() + 1);
        } else {
            TagInfo tagInfo = new TagInfo();
            tagInfo.setIndex(tagMap.size() + 1);
            tagInfo.setType("6B");
            tagInfo.setEpc(logBase6bInfo.getTid());
            tagInfo.setUserData(logBase6bInfo.getUserdata());
            tagInfo.setCount(1);
            tagInfo.setReadTime(new Date());
            tagMap.put(logBase6bInfo.getTid(), tagInfo);
        }
        handler.sendEmptyMessage(0);
    }

    Timer timer;
    long time = 0;
    int speed = 0;

    private void readTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                time += 10;
                handler.sendEmptyMessage(2);
                if (time % 1000 == 0) {
                    synchronized (tagMap) {
                        int sum = tagMap.values().stream().mapToInt(TagInfo::getCount).sum();
                        int rate = sum - speed;
                        speed = sum;
                        handler.sendMessage(handler.obtainMessage(3, rate, 0));
                    }
                }
            }
        }, 0, 10);
    }

    @OnClick(R.id.inventory_btn)
    public void inventoryEvent() {
        if (inventory_btn.getText().equals(getString(R.string.read_btn))) {
            cleanEvent();
            onTagCallback();
            client.sendUnsynMsg(new MsgBaseStop());
            MsgBaseInventory6b msg = new MsgBaseInventory6b();
            msg.setAntennaEnable(EnumG.AntennaNo_1);
            msg.setInventoryMode(mmkv.decodeBool("scan_single_mode", false) ? EnumG.InventoryMode_Single : EnumG.InventoryMode_Inventory);
            msg.setArea(EnumG.ReadMode6b_Tid);

            if (mmkv.decodeBool("set_user_box")) {
                String set_user_start = mmkv.decodeString("set_user_start", "0");
                String set_user_len = mmkv.decodeString("set_user_len", "9");
                msg.setReadUserdata(new Param6bReadUserdata(Integer.parseInt(set_user_start), Integer.parseInt(set_user_len)));
                title_user.setVisibility(View.VISIBLE);
                tagAdapter.setShowUser(true);
                msg.setArea(EnumG.ReadMode6b_TidAndUserdata);
            } else {
                title_user.setVisibility(View.GONE);
                tagAdapter.setShowUser(false);
            }

            String content = match_read_content.getText().toString();
            if (!StringUtils.isNullOfEmpty(content)) {
                msg.setHexMatchTid(content);
            }
            client.sendSynMsg(msg);
            if (msg.getRtCode() == 0) {
                readTimer();
                inventory_btn.setText(getString(R.string.stop_btn));
                if (mmkv.decodeBool("scan_single_mode", false)) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        client.sendUnsynMsg(new MsgBaseStop());
                        if (timer != null) { timer.cancel(); timer = null; }
                        inventory_btn.setText(getString(R.string.read_btn));
                    }, 1200);
                }
            } else {
                Toast.show(msg.getRtMsg());
            }
        } else {
            MsgBaseStop stop = new MsgBaseStop();
            client.sendSynMsg(stop);
            if (stop.getRtCode() == 0) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                inventory_btn.setText(getString(R.string.read_btn));
            } else {
                Toast.show(stop.getRtMsg());
            }
        }
    }

    @OnClick(R.id.inventory_clean)
    public void cleanEvent() {
        tagMap.clear();
        tagList.clear();
        tagTotal.setText("0");
        tagSpeed.setText("0t/s");
        tagReadTime.setText("0ms");
        time = 0;
        speed = 0;
        tagAdapter.notifyDataSetChanged();
        if (clickView != null) {
            clickView.setBackgroundColor(Color.WHITE);
            clickView = null;
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("hidden", hidden + "");
        if (!inventory_btn.getText().equals(getString(R.string.read_btn))) {
            client.sendUnsynMsg(new MsgBaseStop());
            inventory_btn.setText(getString(R.string.read_btn));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy");
    }

    public void onKeyDown(int keyCode){
        switch (keyCode){
            case 619:
                inventoryEvent();
                break;
        }
    }




}



