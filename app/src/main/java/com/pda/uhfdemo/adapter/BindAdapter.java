package com.pda.uhfdemo.adapter;




import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.BindInfo;
import com.pda.uhfdemo.entity.TagInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BindAdapter extends BaseQuickAdapter<BindInfo, BaseViewHolder> {

    public BindAdapter(@Nullable List<BindInfo> data) {
        super(R.layout.bind_view, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, BindInfo info) {
        baseViewHolder.setText(R.id.bind_item_epc, info.getEpc());
        baseViewHolder.setText(R.id.bind_item_name, info.getName());
    }
}
