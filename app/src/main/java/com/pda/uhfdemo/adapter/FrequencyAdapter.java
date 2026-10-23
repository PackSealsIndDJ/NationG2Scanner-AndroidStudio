package com.pda.uhfdemo.adapter;


import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.FrequencyActivity;
import com.pda.uhfdemo.R;

import java.util.List;

public class FrequencyAdapter extends BaseQuickAdapter<FrequencyActivity.PointInfo, BaseViewHolder> {

    public FrequencyAdapter(@Nullable List<FrequencyActivity.PointInfo> data) {
        super(R.layout.frequency_view, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, FrequencyActivity.PointInfo point) {
        baseViewHolder.setText(R.id.freq_point, point.getText());
        ((CheckBox) baseViewHolder.getView(R.id.freq_point)).setChecked(point.isChecked());
        ((CheckBox) baseViewHolder.getView(R.id.freq_point)).setOnCheckedChangeListener((compoundButton, b) -> {
            point.setChecked(b);
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
