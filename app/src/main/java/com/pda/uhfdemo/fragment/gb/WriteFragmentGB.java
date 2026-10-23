package com.pda.uhfdemo.fragment.gb;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.MsgBaseDestroyGJb;
import com.gg.reader.api.protocol.gx.MsgBaseDestroyGb;
import com.gg.reader.api.protocol.gx.MsgBaseLockGJb;
import com.gg.reader.api.protocol.gx.MsgBaseLockGb;
import com.gg.reader.api.protocol.gx.MsgBaseWriteGJb;
import com.gg.reader.api.protocol.gx.MsgBaseWriteGb;
import com.gg.reader.api.protocol.gx.ParamEpcFilter;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.TagInfo;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.PcUtils;
import com.pda.uhfdemo.utils.Toast;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WriteFragmentGB extends Fragment {


    @BindView(R.id.write_epc_spinner)
    Spinner write_epc_spinner;
    @BindView(R.id.write_area_spinner)
    Spinner write_area_spinner;
    @BindView(R.id.write_start_editText)
    EditText write_start_editText;
    @BindView(R.id.write_accessPwd_editText)
    EditText write_accessPwd_editText;
    @BindView(R.id.write_safe_spinner)
    Spinner write_safe_spinner;

    @BindView(R.id.safe_layout)
    LinearLayout safeLayout;
    @BindView(R.id.write_content_editText)
    EditText write_content_editText;
    @BindView(R.id.lock_area_spinner)
    Spinner lock_area_spinner;
    @BindView(R.id.lock_type_spinner)
    Spinner lock_type_spinner;
    @BindView(R.id.lock_pwd_editText)
    EditText lock_pwd_editText;
    @BindView(R.id.destroy_pwd_editText)
    EditText destroy_pwd_editText;

    GClient client = GlobalClient.getClient();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_gb, container, false);
        ButterKnife.bind(this, view);
        initListener();
        return view;
    }

    private void initListener() {
        write_area_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 1:
                        safeLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        write_start_editText.setText("0");
                        write_safe_spinner.setSelection(0);
                        safeLayout.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        write_safe_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        write_start_editText.setText("0");
                        break;
                    case 1:
                        write_start_editText.setText("2");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setData(List<TagInfo> tagInfoList) {
        List<TagInfo> notEmptyList = tagInfoList.stream().filter(tagInfo -> !StringUtils.isNullOfEmpty(tagInfo.getEpc())).collect(Collectors.toList());
        List<String> collect = notEmptyList.stream().map(TagInfo::getEpc).collect(Collectors.toList());
        collect.add(0, getString(R.string.write_hint_1));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, collect);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        write_epc_spinner.setAdapter(adapter);
    }

    public void selectData(int position) {
        write_epc_spinner.setSelection(position);
    }


    private ParamEpcFilter buildFilter() {
        if (write_epc_spinner.getSelectedItem() != null && write_epc_spinner.getSelectedItemPosition() != 0) {
            String epc = write_epc_spinner.getSelectedItem().toString();
            ParamEpcFilter filter = new ParamEpcFilter();
            //0x00.tid 0x10.epc 0x20.safe 0x30-0x3F.user
            filter.setArea(0x10);
            filter.setBitStart(16);
            filter.setBitLength(epc.length() * 4);
            filter.setHexData(epc);
            return filter;
        }
        return null;
    }

    @OnClick(R.id.write_btn)
    public void writeEvent() {
        MsgBaseWriteGb msg = new MsgBaseWriteGb();
        msg.setAntennaEnable(EnumG.AntennaNo_1);
        String start = write_start_editText.getText().toString();
        if (StringUtils.isNullOfEmpty(start)) {
            Toast.show(getString(R.string.write_hint_2));
            return;
        }
        String hexData = write_content_editText.getText().toString();
        if (StringUtils.isNullOfEmpty(hexData)) {
            Toast.show(getString(R.string.write_hint_3));
            return;
        }

        //0x10 epc,0x20 safe,0x30-0x3F user
        int area = write_area_spinner.getSelectedItemPosition();
        switch (area) {
            case 0:
                msg.setArea(0x10);
                break;
            case 1:
                msg.setArea(0x20);
                break;
            default:
                msg.setArea(0x30 + (area - 2));
                break;
        }
        msg.setStart(Integer.parseInt(start));

        int valueLen = PcUtils.getValueLen(hexData);

        if (msg.getArea() == 0x10 && msg.getStart() == 0) {
            hexData = PcUtils.getGbPc(valueLen) + PcUtils.padLeft(hexData, valueLen * 4, '0');
        }
        msg.setHexWriteData(hexData);
        msg.setHexPassword(write_accessPwd_editText.getText().toString());
        msg.setFilter(buildFilter());
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.lock_btn)
    public void lockEvent() {
        String pwd = lock_pwd_editText.getText().toString();
        if (StringUtils.isNullOfEmpty(pwd)) {
            Toast.show(getString(R.string.write_hint_4));
            return;
        }
        MsgBaseLockGb msg = new MsgBaseLockGb();
        msg.setAntennaEnable(EnumG.AntennaNo_1);
        int lock_area = lock_area_spinner.getSelectedItemPosition();
        //0x00 tid,0x10 epc,0x20 safe,0x30-0x3F user
        switch (lock_area) {
            case 0:
                msg.setArea(0x00);
                break;
            case 1:
                msg.setArea(0x10);
                break;
            case 2:
                msg.setArea(0x20);
                break;
            default:
                msg.setArea(0x30 + (lock_area - 3));
                break;
        }

        msg.setLockParam(lock_type_spinner.getSelectedItemPosition());
        msg.setFilter(buildFilter());
        msg.setHexPassword(pwd);
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.destroy_btn)
    public void destroyEvent() {
        String pwd = destroy_pwd_editText.getText().toString();
        if (StringUtils.isNullOfEmpty(pwd)) {
            Toast.show(getString(R.string.write_hint_5));
            return;
        }
        MsgBaseDestroyGb msg = new MsgBaseDestroyGb();
        msg.setAntennaEnable(EnumG.AntennaNo_1);
        msg.setHexPassword(pwd);
        msg.setFilter(buildFilter());
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }


}