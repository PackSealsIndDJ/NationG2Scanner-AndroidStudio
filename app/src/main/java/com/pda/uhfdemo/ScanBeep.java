package com.pda.uhfdemo;

import android.media.AudioManager;
import android.media.ToneGenerator;

/** Short, non-blocking confirmation beep for each RFID inventory callback. */
public final class ScanBeep {
    private static final ToneGenerator TONE = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75);
    private ScanBeep() { }
    public static void play() { TONE.startTone(ToneGenerator.TONE_PROP_BEEP, 75); }
}
