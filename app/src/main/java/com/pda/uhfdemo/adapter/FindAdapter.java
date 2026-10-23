package com.pda.uhfdemo.adapter;




import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.BindInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FindAdapter extends BaseQuickAdapter<BindInfo, BaseViewHolder> {

    public FindAdapter(@Nullable List<BindInfo> data) {
        super(R.layout.find_view, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, BindInfo info) {
        baseViewHolder.setText(R.id.find_item_epc, info.getEpc());
        baseViewHolder.setText(R.id.find_item_name, info.getName());
    }
}
