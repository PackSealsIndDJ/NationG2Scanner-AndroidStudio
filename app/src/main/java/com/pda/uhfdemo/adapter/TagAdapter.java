package com.pda.uhfdemo.adapter;


import android.graphics.Color;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.pda.uhfdemo.R;
import com.pda.uhfdemo.entity.TagInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TagAdapter extends BaseQuickAdapter<TagInfo, BaseViewHolder> {

    private boolean showTid;
    private boolean showUser;
    private boolean showReserved;

    public TagAdapter(@Nullable List<TagInfo> data) {
        super(R.layout.tag_view, data);
    }

    public boolean isShowTid() {
        return showTid;
    }

    public void setShowTid(boolean showTid) {
        this.showTid = showTid;
    }

    public boolean isShowUser() {
        return showUser;
    }

    public void setShowUser(boolean showUser) {
        this.showUser = showUser;
    }

    public boolean isShowReserved() {
        return showReserved;
    }

    public void setShowReserved(boolean showReserved) {
        this.showReserved = showReserved;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, TagInfo info) {
        baseViewHolder.setText(R.id.tag_index, info.getIndex() + "");
        baseViewHolder.setText(R.id.tag_epc, info.getEpc());
        if (showTid) {
            baseViewHolder.getView(R.id.tag_tid).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_tid, info.getTid());
        } else {
            baseViewHolder.getView(R.id.tag_tid).setVisibility(View.GONE);
        }
        if (showUser) {
            baseViewHolder.getView(R.id.tag_user).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_user, info.getUserData());
        } else {
            baseViewHolder.getView(R.id.tag_user).setVisibility(View.GONE);
        }
        if (showReserved) {
            baseViewHolder.getView(R.id.tag_reserved).setVisibility(View.VISIBLE);
            baseViewHolder.setText(R.id.tag_reserved, info.getReserved());
        } else {
            baseViewHolder.getView(R.id.tag_reserved).setVisibility(View.GONE);
        }

        if (info.isErrorTag()) {
            baseViewHolder.setBackgroundColor(R.id.tag_epc, Color.RED);
        } else {
            baseViewHolder.setBackgroundColor(R.id.tag_epc, Color.WHITE);
        }

//        baseViewHolder.setText(R.id.tag_rssi, info.getEpc());
        baseViewHolder.setText(R.id.tag_count, info.getCount() + "");


    }
}
