package com.pda.uhfdemo.fragment.tl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pda.uhfdemo.R;

import butterknife.ButterKnife;


public class WriteFragmentTL extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_tl, container, false);
        ButterKnife.bind(this, view);

        return view;
    }



}