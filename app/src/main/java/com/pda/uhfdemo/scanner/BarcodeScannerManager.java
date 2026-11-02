package com.pda.uhfdemo.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.bld.ScanManager;
import android.bld.scan.configuration.ScanConfig;
import android.bld.scan.configuration.ScannerType;

import java.nio.charset.StandardCharsets;

/** Owns the Nation G2 barcode scanner connection, decode mode, result receiver, and teardown. */
public final class BarcodeScannerManager {
    private static final String TAG = "BarcodeScannerManager";
    private static final String ACTION_BARCODE_RESULT =
            "com.pda.uhfdemo.action.BARCODE_RESULT";

    public enum ScanMode {
        ONE_D,
        TWO_D
    }

    public interface Listener {
        void onBarcodeResult(String value, String symbology);
        void onError(String message, Throwable cause);
    }

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_BARCODE_RESULT.equals(intent.getAction())) {
                return;
            }
            synchronized (BarcodeScannerManager.this) {
                if (!decoding) {
                    return;
                }
            }
            dispatchResult(intent);
        }
    };

    private Listener listener;
    private ScanManager scanManager;
    private boolean receiverRegistered;
    private boolean decoderConnected;
    private boolean scannerOpen;
    private boolean decoding;
    private ScanMode activeMode;

    public BarcodeScannerManager(Context context, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
    }

    public synchronized void setListener(Listener listener) {
        this.listener = listener;
    }

    public synchronized boolean initialize() {
        if (scanManager != null) {
            return true;
        }
        try {
            scanManager = ScanManager.getDefaultInstance(appContext);
            if (scanManager == null) {
                return reportError("Nation G2 scanner SDK is unavailable.", null);
            }
            scanManager.setBroadcastAction(ACTION_BARCODE_RESULT);
            scanManager.setBroadcastKey(ScanConfig.BARCODE_DATA);
            registerResultReceiver();
            scanManager.initSymbologySettings();
            return true;
        } catch (RuntimeException | LinkageError error) {
            unregisterResultReceiver();
            scanManager = null;
            return reportError("Could not initialize the Nation G2 scanner.", error);
        }
    }

    public synchronized boolean startScanning(ScanMode mode) {
        if (mode == null) {
            return reportError("A barcode scan mode is required.", null);
        }
        if (!initialize()) {
            return false;
        }

        try {
            if (scanManager.getScannerType() == ScannerType.SCAN_TYPE_ID_NONE) {
                return reportError("No Nation G2 barcode scanner was detected.", null);
            }
            if (scanManager.isScannerConflicted()) {
                return reportError("The barcode scanner is in use by another application.", null);
            }
            if (decoding && activeMode != mode && !stopScanning()) {
                return false;
            }

            if (!decoderConnected) {
                scanManager.DecoderConnect();
                decoderConnected = true;
            }
            if (!scannerOpen) {
                scannerOpen = scanManager.openScanner();
                if (!scannerOpen) {
                    disconnectDecoder();
                    return reportError("Could not open the Nation G2 barcode scanner.", null);
                }
            }

            boolean oneDimensional = mode == ScanMode.ONE_D;
            scanManager.enableSYM1D(oneDimensional);
            scanManager.enableSYM2D(!oneDimensional);
            scanManager.setBroadcastAction(ACTION_BARCODE_RESULT);
            scanManager.setBroadcastKey(ScanConfig.BARCODE_DATA);

            if (!decoding) {
                decoding = scanManager.startDecode();
                if (!decoding) {
                    closeScanner();
                    disconnectDecoder();
                    return reportError("The Nation G2 scanner could not start decoding.", null);
                }
                activeMode = mode;
            }
            return true;
        } catch (RuntimeException | LinkageError error) {
            stopAndCloseScanner();
            return reportError("Could not start barcode scanning.", error);
        }
    }

    public synchronized boolean stopScanning() {
        if (!decoding || scanManager == null) {
            return true;
        }
        try {
            if (!scanManager.stopDecode()) {
                return reportError("The Nation G2 scanner did not stop decoding.", null);
            }
            decoding = false;
            activeMode = null;
            return true;
        } catch (RuntimeException | LinkageError error) {
            return reportError("Could not stop barcode scanning.", error);
        }
    }

    public synchronized void pause() {
        if (scanManager != null) {
            stopAndCloseScanner();
        }
    }

    public synchronized void release() {
        if (scanManager != null) {
            stopAndCloseScanner();
        }
        unregisterResultReceiver();
        scanManager = null;
        listener = null;
    }

    private void registerResultReceiver() {
        if (receiverRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter(ACTION_BARCODE_RESULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(resultReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            appContext.registerReceiver(resultReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void unregisterResultReceiver() {
        if (!receiverRegistered) {
            return;
        }
        try {
            appContext.unregisterReceiver(resultReceiver);
        } catch (IllegalArgumentException error) {
            Log.w(TAG, "Barcode result receiver was already unregistered.", error);
        } finally {
            receiverRegistered = false;
        }
    }

    private void dispatchResult(Intent intent) {
        try {
            Object value = readExtra(intent, ScanConfig.BARCODE_DATA);
            if (value == null) {
                value = readExtra(intent, ScanConfig.BARCODE_BYTE_DATA);
            }
            if (value == null) {
                value = readExtra(intent, ScanConfig.GEEK_BARCODE_BYTE_DATA);
            }
            String barcode = toText(value);
            if (barcode == null || barcode.isEmpty()) {
                reportError("The scanner returned an empty barcode.", null);
                return;
            }
            Object type = readExtra(intent, ScanConfig.BARCODE_TYPE);
            if (type == null) {
                type = readExtra(intent, ScanConfig.BARCODE_ID);
            }
            final String symbology = type == null ? "" : String.valueOf(type);
            final Listener currentListener = listener;
            if (currentListener != null) {
                mainHandler.post(() -> {
                    if (listener == currentListener) {
                        currentListener.onBarcodeResult(barcode, symbology);
                    }
                });
            }
        } catch (RuntimeException | LinkageError error) {
            reportError("Could not read the barcode result from the Nation G2 scanner.", error);
        }
    }

    private Object readExtra(Intent intent, String key) {
        return intent.getExtras() == null ? null : intent.getExtras().get(key);
    }

    private String toText(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value == null ? null : String.valueOf(value);
    }

    private void stopAndCloseScanner() {
        if (scanManager == null) {
            return;
        }
        if (decoding) {
            try {
                if (!scanManager.stopDecode()) {
                    reportError("The Nation G2 scanner did not stop during cleanup.", null);
                }
            } catch (RuntimeException | LinkageError error) {
                reportError("Could not stop barcode decoding during cleanup.", error);
            }
            decoding = false;
            activeMode = null;
        }
        closeScanner();
        disconnectDecoder();
    }

    private void closeScanner() {
        if (!scannerOpen || scanManager == null) {
            return;
        }
        try {
            if (!scanManager.closeScanner()) {
                reportError("The Nation G2 scanner did not close cleanly.", null);
            }
        } catch (RuntimeException | LinkageError error) {
            reportError("Could not close the Nation G2 barcode scanner.", error);
        } finally {
            scannerOpen = false;
        }
    }

    private void disconnectDecoder() {
        if (!decoderConnected || scanManager == null) {
            return;
        }
        try {
            scanManager.DecoderDisconnect();
        } catch (RuntimeException | LinkageError error) {
            reportError("Could not disconnect the Nation G2 barcode scanner.", error);
        } finally {
            decoderConnected = false;
        }
    }

    private boolean reportError(String message, Throwable cause) {
        Log.e(TAG, message, cause);
        final Listener currentListener = listener;
        if (currentListener != null) {
            mainHandler.post(() -> {
                if (listener == currentListener) {
                    currentListener.onError(message, cause);
                }
            });
        }
        return false;
    }
}
