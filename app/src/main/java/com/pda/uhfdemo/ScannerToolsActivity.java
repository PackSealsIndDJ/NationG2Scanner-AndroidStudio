package com.pda.uhfdemo;

import android.Manifest;
import android.app.Activity;
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

import java.util.Locale;

/** Android 13 tools: camera barcode reader, NFC reader mode, one-shot GPS, and device details. */
public class ScannerToolsActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 4101;
    private static final int REQUEST_LOCATION = 4102;
    private static final int REQUEST_BARCODE = 4103;
    private static final String[] ONE_D = {"UPC_A", "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93", "CODE_128", "ITF", "RSS_14", "RSS_EXPANDED"};
    private static final String[] TWO_D = {"QR_CODE", "DATA_MATRIX", "PDF_417", "AZTEC"};

    private TextView output;
    private NfcAdapter nfcAdapter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String[] pendingBarcodeFormats;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable locationTimeout = () -> show("GPS", "No GPS fix yet. Move outdoors and try again.");

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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
        addButton(column, "Scan 1D barcode", v -> launchBarcode(ONE_D));
        addButton(column, "Scan 2D barcode", v -> launchBarcode(TWO_D));
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

    private void launchBarcode(String[] formats) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pendingBarcodeFormats = formats;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            return;
        }
        try {
            Intent scan = new Intent(this, com.uuzuche.lib_zxing.activity.CaptureActivity.class);
            scan.putExtra("SCAN_FORMATS", join(formats));
            startActivityForResult(scan, REQUEST_BARCODE);
        } catch (Exception e) {
            show("Barcode scanner", "Scanner could not start: " + e.getMessage());
        }
    }

    private String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) { if (result.length() > 0) result.append(','); result.append(value); }
        return result.toString();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BARCODE && resultCode == Activity.RESULT_OK && data != null) {
            String value = data.getStringExtra("codedContent");
            if (value == null || value.isEmpty()) value = data.getStringExtra("SCAN_RESULT");
            if (value != null && !value.isEmpty()) { ScanBeep.play(); show("Barcode result", value); }
            else show("Barcode result", "No barcode was returned.");
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String[] formats = pendingBarcodeFormats;
            pendingBarcodeFormats = null;
            if (formats != null) launchBarcode(formats);
        } else if (requestCode == REQUEST_CAMERA) {
            show("Barcode scanner", "Camera permission was denied.");
        } else if (requestCode == REQUEST_LOCATION) {
            boolean locationGranted = false;
            for (int result : grantResults) locationGranted |= result == PackageManager.PERMISSION_GRANTED;
            if (locationGranted) requestGps();
            else show("GPS", "Location permission was denied.");
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
            String provider = fineGranted ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
            if (!locationManager.isProviderEnabled(provider)) {
                show("Location", "Location services are turned off. Enable Location in Android Settings and try again.");
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            Location last = locationManager.getLastKnownLocation(provider);
            if (last != null && System.currentTimeMillis() - last.getTime() < 120000) { showLocation(last); return; }
            show("GPS", "Waiting up to 20 seconds for a GPS fix…");
            locationListener = new LocationListener() {
                @Override public void onLocationChanged(@NonNull Location location) { locationManager.removeUpdates(this); handler.removeCallbacks(locationTimeout); showLocation(location); }
            };
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener, Looper.getMainLooper());
            handler.removeCallbacks(locationTimeout);
            handler.postDelayed(() -> { if (locationListener != null) { locationManager.removeUpdates(locationListener); locationListener = null; locationTimeout.run(); } }, 20000);
        } catch (SecurityException e) { show("GPS", "Location permission is required."); }
        catch (Exception e) { show("GPS", "Could not read GPS: " + e.getMessage()); }
    }

    private void showLocation(Location location) {
        show("GPS location", String.format(Locale.US, "Latitude: %.6f\nLongitude: %.6f\nAccuracy: %.1f m\nTime: %tF %tT", location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime(), location.getTime()));
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
        if (locationListener != null) { locationManager.removeUpdates(locationListener); locationListener = null; }
        handler.removeCallbacks(locationTimeout);
        super.onPause();
    }

    private String deviceInfo() {
        return "Manufacturer: " + Build.MANUFACTURER + "\nModel: " + Build.MODEL + "\nDevice: " + Build.DEVICE + "\nProduct: " + Build.PRODUCT + "\nAndroid: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\nBuild: " + Build.DISPLAY + "\nABI: " + Build.SUPPORTED_ABIS[0] + "\nUHF: Nation G2 reader.jar SDK bundled\nTrigger key: 619 (vendor demo mapping)\nNFC: " + (nfcAdapter == null ? "Not available" : nfcAdapter.isEnabled() ? "Available / enabled" : "Available / disabled") + "\nGPS: " + getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) + "\nCamera: " + getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void show(String title, String message) {
        if (output != null) output.setText(title + "\n\n" + message);
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
}
