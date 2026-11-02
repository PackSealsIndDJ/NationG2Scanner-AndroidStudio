package com.pda.uhfdemo;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

/** Short, non-blocking confirmation beep for scanner and RFID callbacks. */
public final class ScanBeep {
    private static final String TAG = "ScanBeep";
    private static ToneGenerator tone;

    private ScanBeep() { }

    public static synchronized void play() {
        try {
            if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 75);
        } catch (RuntimeException error) {
            Log.w(TAG, "Could not play scanner confirmation tone.", error);
            ToneGenerator failedTone = tone;
            tone = null;
            if (failedTone != null) {
                try {
                    failedTone.release();
                } catch (RuntimeException releaseError) {
                    Log.w(TAG, "Could not release the scanner confirmation tone.", releaseError);
                }
            }
        }
    }
}
