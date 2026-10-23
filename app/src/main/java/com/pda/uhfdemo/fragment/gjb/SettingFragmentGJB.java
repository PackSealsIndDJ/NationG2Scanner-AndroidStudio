package com.pda.uhfdemo.fragment.gjb;

import android.bld.DeviceManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.MsgAppGetBaseVersion;
import com.gg.reader.api.protocol.gx.MsgAppGetReaderInfo;
import com.gg.reader.api.protocol.gx.MsgBaseGetAutoDormancy;
import com.gg.reader.api.protocol.gx.MsgBaseGetBaseband;
import com.gg.reader.api.protocol.gx.MsgBaseGetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseGetGJbBaseband;
import com.gg.reader.api.protocol.gx.MsgBaseGetPower;
import com.gg.reader.api.protocol.gx.MsgBaseSetAutoDormancy;
import com.gg.reader.api.protocol.gx.MsgBaseSetBaseband;
import com.gg.reader.api.protocol.gx.MsgBaseSetFreqRange;
import com.gg.reader.api.protocol.gx.MsgBaseSetGJbBaseband;
import com.gg.reader.api.protocol.gx.MsgBaseSetPower;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.ParamGbAntiCollision;
import com.gg.reader.api.protocol.gx.ParamGbBaseSpeed;
import com.gg.reader.api.utils.StringUtils;
import com.pda.uhfdemo.MainActivity;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.utils.AppUtils;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.QrCodeUtils;
import com.pda.uhfdemo.utils.Toast;
import com.tencent.mmkv.MMKV;

import java.util.Hashtable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingFragmentGJB extends Fragment {

    @BindView(R.id.set_tid_box)
    CheckBox set_tid_box;
    @BindView(R.id.set_tid_mode)
    Spinner set_tid_mode;
    @BindView(R.id.set_tid_len)
    EditText set_tid_len;
    @BindView(R.id.set_user_box)
    CheckBox set_user_box;
    @BindView(R.id.set_user_start)
    EditText set_user_start;
    @BindView(R.id.set_user_len)
    EditText set_user_len;
    @BindView(R.id.set_power_spinner)
    Spinner set_power_spinner;
    @BindView(R.id.set_base_speed)
    Spinner set_base_speed;
    @BindView(R.id.set_base_trext)
    Spinner set_base_trext;
    @BindView(R.id.set_base_kValue)
    Spinner set_base_kValue;
    @BindView(R.id.set_base_miller)
    Spinner set_base_miller;
    @BindView(R.id.set_base_cin)
    EditText set_base_cin;
    @BindView(R.id.set_base_ccn)
    EditText set_base_ccn;
    @BindView(R.id.set_base_ab)
    Spinner set_base_ab;



    @BindView(R.id.set_info_baseVersion)
    TextView set_info_baseVersion;
    @BindView(R.id.set_info_serialNo)
    TextView set_info_serialNo;

    @BindView(R.id.set_info_powerTime)
    TextView set_info_powerTime;

    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.set_info_baseCompareTime)
    TextView set_info_baseCompareTime;

    @BindView(R.id.version_Layout)
    LinearLayout version_Layout;

    GClient client = GlobalClient.getClient();

    MMKV mmkv = MMKV.defaultMMKV();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_gjb, container, false);
        ButterKnife.bind(this, view);
        initListener();
        initTagProtocol();
        return view;
    }


    private void initTagProtocol() {
        int tagProtocol = mmkv.decodeInt("set_tag_protocol", 0);
        switch (tagProtocol) {
            case 0:
                radioGroup.check(R.id.radio_6c);
                break;
            case 1:
                radioGroup.check(R.id.radio_6b);
                break;
            case 2:
                radioGroup.check(R.id.radio_gb);
                break;
            case 3:
                radioGroup.check(R.id.radio_gjb);
                break;
            case 4:
                radioGroup.check(R.id.radio_tl);
                break;
        }
        radioGroup.setOnCheckedChangeListener((radio, i) -> {
            switch (i) {
                case R.id.radio_6c:
                    mmkv.encode("set_tag_protocol", 0);
                    AppUtils.reset(getActivity());
                    break;
                case R.id.radio_6b:
                    mmkv.encode("set_tag_protocol", 1);
                    AppUtils.reset(getActivity());
                    break;
                case R.id.radio_gb:
                    mmkv.encode("set_tag_protocol", 2);
                    AppUtils.reset(getActivity());
                    break;
                case R.id.radio_gjb:
                    mmkv.encode("set_tag_protocol", 3);
                    AppUtils.reset(getActivity());
                    break;
                case R.id.radio_tl:
                    mmkv.encode("set_tag_protocol", 4);
                    AppUtils.reset(getActivity());
                    break;

            }
        });
    }
    private void initListener() {
        set_tid_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mmkv.encode("set_tid_box", b);
            }
        });
        set_tid_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mmkv.encode("set_tid_mode", i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        set_tid_len.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mmkv.encode("set_tid_len", charSequence == null ? "6" : charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        set_user_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mmkv.encode("set_user_box", b);
            }
        });
        set_user_start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mmkv.encode("set_user_start", charSequence == null ? "0" : charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        set_user_len.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mmkv.encode("set_user_len", charSequence == null ? "2" : charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        set_tid_box.setChecked(mmkv.decodeBool("set_tid_box", false));
        set_tid_mode.setSelection(mmkv.decodeInt("set_tid_mode", 0));
        set_tid_len.setText(mmkv.decodeString("set_tid_len", "6"));

        set_user_box.setChecked(mmkv.decodeBool("set_user_box", false));
        set_user_start.setText(mmkv.decodeString("set_user_start", "0"));
        set_user_len.setText(mmkv.decodeString("set_user_len", "2"));

        version_Layout.setOnClickListener(view -> {
            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.qr_code_view, null);
            ImageView serialNo_img = inflate.findViewById(R.id.serialNo_img);
            TextView serialNo_text = inflate.findViewById(R.id.serialNo_text);
            serialNo_text.setText(set_info_serialNo.getText());
            serialNo_img.setImageBitmap(QrCodeUtils.getQrCode(set_info_serialNo.getText().toString()));

            ImageView imei_img = inflate.findViewById(R.id.imei_img);
            TextView mei_text = inflate.findViewById(R.id.imei_text);
            DeviceManager deviceManager = new DeviceManager();
            String imei1 = deviceManager.getIMEI(1);
            Log.i("TAG", "IMEI1：" + imei1);
            mei_text.setText(imei1);
            imei_img.setImageBitmap(QrCodeUtils.getQrCode(imei1));

            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("QrCode")
                    .setView(inflate)
                    .create();
            alertDialog.show();
        });
    }

    public void initQuery() {
        MsgBaseStop stop = new MsgBaseStop();
        client.sendSynMsg(stop, 1500);
        if (stop.getRtCode() == 0) {
            infoQueryEvent();
            powerQueryEvent();
            baseQueryEvent();
        }
    }

    @OnClick(R.id.power_query_btn)
    public void powerQueryEvent() {
        MsgBaseGetPower msg = new MsgBaseGetPower();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            set_power_spinner.setSelection(msg.getDicPower().get(1));
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.power_set_btn)
    public void powerSetEvent() {
        MsgBaseSetPower msg = new MsgBaseSetPower();
        Hashtable<Integer, Integer> hashtable = new Hashtable<>();
        hashtable.put(1, set_power_spinner.getSelectedItemPosition());
        msg.setDicPower(hashtable);
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.base_query_btn)
    public void baseQueryEvent() {
        MsgBaseGetGJbBaseband msg = new MsgBaseGetGJbBaseband();
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            ParamGbBaseSpeed baseSpeed = msg.getParamGbBaseSpeed();
            set_base_speed.setSelection(baseSpeed.getTc());
            set_base_trext.setSelection(baseSpeed.getLS());
            set_base_kValue.setSelection(baseSpeed.getRLF());
            set_base_miller.setSelection(baseSpeed.getRLC());

            set_base_cin.setText(msg.getParamGbAntiCollision().getCIN()+"");
            set_base_ccn.setText(msg.getParamGbAntiCollision().getCCN()+"");

            set_base_ab.setSelection(msg.getInventoryFlag());
        } else {
            Toast.show(msg.getRtMsg());
        }
    }

    @OnClick(R.id.base_set_btn)
    public void baseSetEvent() {
        MsgBaseSetGJbBaseband msg = new MsgBaseSetGJbBaseband();

        ParamGbBaseSpeed baseSpeed = new ParamGbBaseSpeed();
        baseSpeed.setTc(set_base_speed.getSelectedItemPosition());
        baseSpeed.setLS(set_base_trext.getSelectedItemPosition());
        baseSpeed.setRLF(set_base_kValue.getSelectedItemPosition());
        baseSpeed.setRLC(set_base_miller.getSelectedItemPosition());
        msg.setParamGbBaseSpeed(baseSpeed);

        msg.setParamGbAntiCollision(new ParamGbAntiCollision(Integer.parseInt(set_base_cin.getText().toString()),Integer.parseInt(set_base_ccn.getText().toString())));

        msg.setInventoryFlag(set_base_ab.getSelectedItemPosition());
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }


    public void infoQueryEvent() {
        MsgAppGetBaseVersion getBaseVersion = new MsgAppGetBaseVersion();
        client.sendSynMsg(getBaseVersion);
        if (getBaseVersion.getRtCode() == 0) {
            set_info_baseVersion.setText(getBaseVersion.getBaseVersions());
        } else {
            Toast.show(getBaseVersion.getRtMsg());
        }

        MsgAppGetReaderInfo getReaderInfo = new MsgAppGetReaderInfo();
        client.sendSynMsg(getReaderInfo);
        if (getReaderInfo.getRtCode() == 0) {
            set_info_serialNo.setText(getReaderInfo.getReaderSerialNumber());
            set_info_powerTime.setText(getReaderInfo.getFormatPowerOnTime());
            set_info_baseCompareTime.setText(getReaderInfo.getBaseCompileTime());
        } else {
            Toast.show(getBaseVersion.getRtMsg());
        }

    }


}