package com.pda.uhfdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Android 13 tools: Nation G2 barcode reader, NFC reader mode, one-shot GPS, and device details. */
public class ScannerToolsActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 4102;
    private static final int REQUEST_CAMERA = 4103;
    private static final int REQUEST_BARCODE_SCAN = 4104;
    private static final long LOCATION_TIMEOUT_MS = 60000;
    private static final long FRESH_LOCATION_AGE_MS = 30000;
    private static final long MAX_CACHED_LOCATION_AGE_MS = 120000;

    private TextView output;
    private NfcAdapter nfcAdapter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location bestLocation;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable locationTimeout = () -> {
        stopLocationUpdates();
        if (bestLocation != null) showLocation(bestLocation);
        else show("GPS", "No location fix yet. Move outdoors and try again.");
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ZXingLibrary.initDisplayOpinion(this);
        buildUi();
    }

    private void buildUi() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(22, 18, 22, 18);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(column);
        addButton(column, "Scan 1D barcode", v -> launchCameraScanner());
        addButton(column, "Scan 2D barcode", v -> launchCameraScanner());
        addButton(column, "Read NFC tag", v -> show("NFC", nfcAdapter == null ? "This device has no NFC adapter." : nfcAdapter.isEnabled() ? "NFC is ready. Hold an NFC tag near the handheld." : "NFC is present but turned off. Enable NFC in Android Settings."));
        addButton(column, "Get GPS location", v -> requestGps());
        addButton(column, "Nation G2 device info", v -> show("Device information", deviceInfo()));
        output = new TextView(this);
        output.setTextSize(16);
        output.setPadding(0, 20, 0, 16);
        output.setText("Ready. Choose a tool above.");
        column.addView(output);
        content.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(content);
        setTitle("Scanner tools");
    }

    private void addButton(LinearLayout parent, String title, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(title);
        button.setOnClickListener(listener);
        parent.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }

    private void launchCameraScanner() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            show("Barcode scanner", "This device does not have a camera.");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        output.setText("Point the camera at a 1D barcode, QR code, or Data Matrix barcode.");
        startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_BARCODE_SCAN);
    }

    private String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append(", ");
            result.append(value);
        }
        return result.toString();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            boolean locationGranted = false;
            for (int result : grantResults) locationGranted |= result == PackageManager.PERMISSION_GRANTED;
            if (locationGranted) requestGps();
            else show("GPS", "Location permission was denied.");
        } else if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraScanner();
            } else {
                show("Barcode scanner", "Camera permission was denied.");
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_BARCODE_SCAN) return;
        if (resultCode != RESULT_OK || data == null) {
            show("Barcode scanner", "Camera scan was cancelled.");
            return;
        }
        int resultType = data.getIntExtra(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
        String value = data.getStringExtra(CodeUtils.RESULT_STRING);
        if (resultType == CodeUtils.RESULT_SUCCESS && value != null && !value.isEmpty()) {
            ScanBeep.play();
            show("Barcode result", value);
        } else {
            show("Barcode scanner", "No barcode was recognized. Try again with the barcode in focus.");
        }
    }

    private void requestGps() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fineGranted && !coarseGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        try {
            stopLocationUpdates();
            handler.removeCallbacks(locationTimeout);
            bestLocation = null;

            List<String> providers = new ArrayList<>();
            if (fineGranted && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                providers.add(LocationManager.GPS_PROVIDER);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                providers.add(LocationManager.NETWORK_PROVIDER);
            }
            if (providers.isEmpty()) {
                show("GPS", "No location provider is enabled. Turn on Location in Android Settings and try again.");
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            long now = System.currentTimeMillis();
            for (String provider : providers) {
                Location last = locationManager.getLastKnownLocation(provider);
                if (isRecentLocation(last, now, MAX_CACHED_LOCATION_AGE_MS)
                        && isBetterLocation(last, bestLocation)) {
                    bestLocation = last;
                }
            }
            if (isRecentLocation(bestLocation, now, FRESH_LOCATION_AGE_MS)
                    && (!fineGranted
                    || LocationManager.GPS_PROVIDER.equals(bestLocation.getProvider())
                    || (bestLocation.hasAccuracy() && bestLocation.getAccuracy() <= 50))) {
                showLocation(bestLocation);
                return;
            }

            show("GPS", "Waiting up to 60 seconds for a location fix…");
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(@NonNull Location location) {
                    if (isBetterLocation(location, bestLocation)) {
                        bestLocation = location;
                        showLocation(bestLocation);
                    }
                    if (LocationManager.GPS_PROVIDER.equals(location.getProvider())
                            && location.hasAccuracy() && location.getAccuracy() <= 50) {
                        stopLocationUpdates();
                        handler.removeCallbacks(locationTimeout);
                    }
                }
            };
            for (String provider : providers) {
                locationManager.requestLocationUpdates(provider, 1000, 0, locationListener, Looper.getMainLooper());
            }
            handler.postDelayed(locationTimeout, LOCATION_TIMEOUT_MS);
        } catch (SecurityException e) { show("GPS", "Location permission is required."); }
        catch (Exception e) { show("GPS", "Could not read GPS: " + e.getMessage()); }
    }

    private boolean isRecentLocation(Location location, long now, long maxAge) {
        return location != null && now - location.getTime() >= 0 && now - location.getTime() <= maxAge;
    }

    private boolean isBetterLocation(Location candidate, Location current) {
        if (current == null) return true;
        if (!candidate.hasAccuracy()) return false;
        return !current.hasAccuracy() || candidate.getAccuracy() < current.getAccuracy();
    }

    private void stopLocationUpdates() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private void showLocation(Location location) {
        String accuracy = location.hasAccuracy()
                ? String.format(Locale.US, "%.1f m", location.getAccuracy())
                : "unknown";
        show("GPS location", String.format(Locale.US, "Latitude: %.6f\nLongitude: %.6f\nAccuracy: %s\nSource: %s\nTime: %tF %tT",
                location.getLatitude(), location.getLongitude(), accuracy, location.getProvider(), location.getTime(), location.getTime()));
    }

    @Override protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try { nfcAdapter.enableReaderMode(this, tag -> runOnUiThread(() -> showNfcTag(tag)), NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, null); }
            catch (SecurityException ignored) { }
        }
    }

    private void showNfcTag(Tag tag) {
        byte[] id = tag.getId();
        StringBuilder hex = new StringBuilder();
        for (byte b : id) hex.append(String.format(Locale.US, "%02X", b));
        ScanBeep.play();
        show("NFC tag", "Tag ID: " + hex + "\nTechnologies: " + join(tag.getTechList()));
    }

    @Override protected void onPause() {
        if (nfcAdapter != null) nfcAdapter.disableReaderMode(this);
        stopLocationUpdates();
        handler.removeCallbacks(locationTimeout);
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    private String deviceInfo() {
        return "Manufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nDevice: " + Build.DEVICE + "\nProduct: " + Build.PRODUCT + "\nAndroid: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\nBuild: " + Build.DISPLAY + "\nABI: " + Build.SUPPORTED_ABIS[0] + "\nUHF: Nation G2 reader.jar SDK bundled\nTrigger key: 619 (vendor demo mapping)\nNFC: " + (nfcAdapter == null ? "Not available" : nfcAdapter.isEnabled() ? "Available / enabled" : "Available / disabled") + "\nGPS: " + getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) + "\nCamera: " + getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void show(String title, String message) {
        if (output != null) output.setText(title + "\n\n" + message);
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
}
