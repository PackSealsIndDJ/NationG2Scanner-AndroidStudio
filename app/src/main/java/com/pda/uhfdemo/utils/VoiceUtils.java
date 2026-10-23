package com.pda.uhfdemo.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.pda.uhfdemo.R;

public class VoiceUtils {
    private static MediaPlayer mPlayer;

    public static void init(Context context) {
        mPlayer = MediaPlayer.create(context, R.raw.shortbibi);
        mPlayer.setVolume(1.0f, 1.0f);
    }

    public static void start() {
        if (mPlayer != null) {
            mPlayer.start();
        }
    }

    public static void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }
}
