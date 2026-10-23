package com.pda.uhfdemo.fragment.gjb;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.coder.zzq.smartshow.dialog.DialogBtnClickListener;
import com.coder.zzq.smartshow.dialog.EnsureDialog;
import com.coder.zzq.smartshow.dialog.SmartDialog;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.adapter.BindAdapter;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.entity.TagInfo;
import com.pda.uhfdemo.utils.Toast;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindFragmentGJB extends Fragment {

    @BindView(R.id.bind_epc)
    Spinner bind_epc;
    @BindView(R.id.bind_name)
    EditText bind_name;
    @BindView(R.id.bind_listView)
    RecyclerView bindListView;

    MMKV mmkv = MMKV.defaultMMKV();

    BindAdapter bindAdapter;

    List<BindInfo> bindInfoList = new ArrayList<>();
    LinkedHashMap<String, BindInfo> bindMap = new LinkedHashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bind_gjb, container, false);
        ButterKnife.bind(this, view);
        initAdapter();
        Log.e("BindFragment","BindFragment");
        return view;
    }

    private void initAdapter() {
        if (mmkv.allKeys() != null) {
            for (String key : mmkv.allKeys()) {
                if (!key.startsWith("set")) {
                    bindMap.put(key, new BindInfo(key, mmkv.decodeString(key)));
                }
            }
        }
        bindInfoList.addAll(bindMap.values());
        bindAdapter = new BindAdapter(bindInfoList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bindListView.addItemDecoration(new DividerItemDecoration(getContext(), 1));
        bindListView.setLayoutManager(layoutManager);
        bindListView.setAdapter(bindAdapter);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setData(List<TagInfo> tagInfoList) {
        List<TagInfo> notEmptyList = tagInfoList.stream().filter(tagInfo -> !StringUtils.isNullOfEmpty(tagInfo.getEpc())).collect(Collectors.toList());
        List<String> collect = notEmptyList.stream().map(TagInfo::getEpc).collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, collect);
//        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bind_epc.setAdapter(adapter);
    }

    public void selectData(int position) {
        bind_epc.setSelection(position);
    }

    @OnClick(R.id.bind_event)
    public void bindEvent() {
        if (bind_epc.getSelectedItem() == null) {
            Toast.show(getString(R.string.bind_hint_1));
            return;
        }
        if (StringUtils.isNullOfEmpty(bind_name.getText().toString())) {
            Toast.show(getString(R.string.bind_hint_2));
            return;
        }

        String epc = bind_epc.getSelectedItem().toString();
        String name = bind_name.getText().toString();
        mmkv.encode(epc, name);
        bindMap.put(epc, new BindInfo(epc, name));
        bindInfoList.clear();
        bindInfoList.addAll(bindMap.values());
        bindAdapter.notifyDataSetChanged();
        Toast.show(getString(R.string.bind_hint_3));
    }

    @OnClick(R.id.bind_clean)
    public void bindClean() {
        EnsureDialog ensureDialog = new EnsureDialog();
        ensureDialog.title(getString(R.string.bind_hint_4));
        ensureDialog.message(getString(R.string.bind_hint_5));
        ensureDialog.confirmBtn(getString(R.string.bind_hint_6), new DialogBtnClickListener() {
            @Override
            public void onBtnClick(SmartDialog dialog, int which, Object data) {
                if (mmkv.allKeys() != null) {
                    for (String key : mmkv.allKeys()) {
                        if (!key.startsWith("set")) {
                            mmkv.remove(key);
                        }
                    }
                }
                bindInfoList.clear();
                bindMap.clear();
                bindAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        ensureDialog.showInFragment(this);

    }

}