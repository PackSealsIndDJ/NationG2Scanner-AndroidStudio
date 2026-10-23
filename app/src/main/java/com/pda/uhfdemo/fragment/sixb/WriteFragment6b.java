package com.pda.uhfdemo.fragment.sixb;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.MsgBaseLock6b;
import com.gg.reader.api.protocol.gx.MsgBaseLock6bGet;
import com.gg.reader.api.protocol.gx.MsgBaseWrite6b;
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


public class WriteFragment6b extends Fragment {


    @BindView(R.id.write_tid_spinner)
    Spinner write_tid_spinner;
    @BindView(R.id.write_start_editText)
    EditText write_start_editText;
    @BindView(R.id.write_content_editText)
    EditText write_content_editText;

    @BindView(R.id.lock_address)
    EditText lock_address;
    @BindView(R.id.lock_address_query)
    EditText lock_address_query;

    GClient client = GlobalClient.getClient();


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_6b, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setData(List<TagInfo> tagInfoList) {
        List<TagInfo> notEmptyList = tagInfoList.stream().filter(tagInfo -> !StringUtils.isNullOfEmpty(tagInfo.getEpc())).collect(Collectors.toList());
        List<String> collect = notEmptyList.stream().map(TagInfo::getEpc).collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, collect);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        write_tid_spinner.setAdapter(adapter);
    }

    public void selectData(int position) {
        write_tid_spinner.setSelection(position);
    }


    @OnClick(R.id.write_btn)
    public void writeEvent() {
        MsgBaseWrite6b msg = new MsgBaseWrite6b();
        msg.setAntennaEnable(EnumG.AntennaNo_1);

        if (write_tid_spinner.getSelectedItemPosition() == -1) {
            Toast.show(getString(R.string.write_6b_hint_1));
            return;
        }

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
        msg.setStart(Integer.parseInt(start));
        int valueLen = PcUtils.getTwoValueLen(hexData);
        hexData = PcUtils.padLeft(hexData, valueLen * 2, '0');
        msg.setHexWriteData(hexData);
        msg.setHexMatchTid((String) write_tid_spinner.getSelectedItem());

        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.lock_btn)
    public void lockEvent() {
        String address = lock_address.getText().toString();
        MsgBaseLock6b msg = new MsgBaseLock6b();
        msg.setAntennaEnable(EnumG.AntennaNo_1);
        msg.setHexMatchTid((String) write_tid_spinner.getSelectedItem());
        msg.setLockIndex(Integer.parseInt(address));
        client.sendSynMsg(msg);
        Toast.show(msg.getRtMsg());
    }

    @OnClick(R.id.lock_query_btn)
    public void lockQueryEvent() {
        String address = lock_address_query.getText().toString();
        MsgBaseLock6bGet msg = new MsgBaseLock6bGet();
        msg.setAntennaEnable(EnumG.AntennaNo_1);
        msg.setHexMatchTid((String) write_tid_spinner.getSelectedItem());
        msg.setLockIndex(Integer.parseInt(address));
        client.sendSynMsg(msg);
        if (msg.getRtCode() == 0) {
            Toast.show(msg.getLockState() == 0 ? "unlock" : "locked");
        } else {
            Toast.show(msg.getRtMsg());
        }
    }


}