package com.pda.uhfdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.utils.StringUtils;
import com.gg.reader.api.utils.ThreadPoolUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gxwl.device.reader.dal.SerialPort;
import com.pda.uhfdemo.fragment.gb.BindFragmentGB;
import com.pda.uhfdemo.fragment.gb.FindFragmentGB;
import com.pda.uhfdemo.fragment.gb.ReadFragmentGB;
import com.pda.uhfdemo.fragment.gb.SettingFragmentGB;
import com.pda.uhfdemo.fragment.gb.WriteFragmentGB;
import com.pda.uhfdemo.fragment.gjb.BindFragmentGJB;
import com.pda.uhfdemo.fragment.gjb.FindFragmentGJB;
import com.pda.uhfdemo.fragment.gjb.ReadFragmentGJB;
import com.pda.uhfdemo.fragment.gjb.SettingFragmentGJB;
import com.pda.uhfdemo.fragment.gjb.WriteFragmentGJB;
import com.pda.uhfdemo.fragment.sixb.BindFragment6b;
import com.pda.uhfdemo.fragment.sixb.FindFragment6b;
import com.pda.uhfdemo.fragment.sixb.ReadFragment6b;
import com.pda.uhfdemo.fragment.sixb.SettingFragment6b;
import com.pda.uhfdemo.fragment.sixb.WriteFragment6b;
import com.pda.uhfdemo.fragment.sixc.BindFragment6c;
import com.pda.uhfdemo.fragment.sixc.FindFragment6c;
import com.pda.uhfdemo.fragment.sixc.ReadFragment6c;
import com.pda.uhfdemo.fragment.sixc.SettingFragment6c;
import com.pda.uhfdemo.fragment.sixc.WriteFragment6c;
import com.pda.uhfdemo.fragment.tl.BindFragmentTL;
import com.pda.uhfdemo.fragment.tl.FindFragmentTL;
import com.pda.uhfdemo.fragment.tl.ReadFragmentTL;
import com.pda.uhfdemo.fragment.tl.SettingFragmentTL;
import com.pda.uhfdemo.fragment.tl.WriteFragmentTL;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.LocalManageUtil;
import com.pda.uhfdemo.utils.PowerUtil;
import com.pda.uhfdemo.utils.Toast;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.nav_view)
    BottomNavigationView navView;
    GClient client = GlobalClient.getClient();
    private final List<Fragment> fragments = new ArrayList<>();
    private int selectedId;
    MMKV mmkv = MMKV.defaultMMKV();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        LocalManageUtil.saveSelectLanguage(this, 0);
        ButterKnife.bind(this);
        findViewById(R.id.open_scanner_tools).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, ScannerToolsActivity.class)));
        android.widget.Button modeButton = findViewById(R.id.scan_mode_button);
        updateScanModeLabel(modeButton);
        modeButton.setOnClickListener(v -> {
            boolean singleMode = !mmkv.decodeBool("scan_single_mode", false);
            mmkv.encode("scan_single_mode", singleMode);
            updateScanModeLabel(modeButton);
        });
        int tagProtocol = mmkv.decodeInt("set_tag_protocol", 0);
        switch (tagProtocol) {
            case 0:
                load6cFragments();
                break;
            case 1:
                load6bFragments();
                break;
            case 2:
                loadGBFragments();
                break;
            case 3:
                loadGJBFragments();
                break;
            case 4:
                loadTLFragments();
                break;
        }
//    init();
    }

    private void updateScanModeLabel(android.widget.Button button) {
        button.setText(mmkv.decodeBool("scan_single_mode", false) ? "Mode: Single" : "Mode: Bulk");
    }

    //加载所有的Fragment
    private void load6cFragments() {
        fragments.add(new ReadFragment6c());
        fragments.add(new BindFragment6c());
        fragments.add(new FindFragment6c());
        fragments.add(new WriteFragment6c());
        fragments.add(new SettingFragment6c());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(R.id.nav_host_fragment_activity_main, fragment, fragment.getClass().getName());
            if (!(fragment instanceof ReadFragment6c)) {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inventory:
                        showFragment(fragments.get(0), fragments.get(selectedId));
                        selectedId = 0;
                        break;
                    case R.id.navigation_bind:
                        showFragment(fragments.get(1), fragments.get(selectedId));
                        selectedId = 1;
                        break;
                    case R.id.navigation_find:
                        showFragment(fragments.get(2), fragments.get(selectedId));
                        selectedId = 2;
                        break;
                    case R.id.navigation_write:
                        client.onTagEpcLog = null;
                        client.onTagEpcOver = null;
                        showFragment(fragments.get(3), fragments.get(selectedId));
                        selectedId = 3;
                        break;
                    case R.id.navigation_setting:
                        SettingFragment6c settingFragment6c = (SettingFragment6c) fragments.get(4);
                        showFragment(settingFragment6c, fragments.get(selectedId));
                        settingFragment6c.initQuery();
                        selectedId = 4;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void load6bFragments() {
        fragments.add(new ReadFragment6b());
        fragments.add(new BindFragment6b());
        fragments.add(new FindFragment6b());
        fragments.add(new WriteFragment6b());
        fragments.add(new SettingFragment6b());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(R.id.nav_host_fragment_activity_main, fragment, fragment.getClass().getName());
            if (!(fragment instanceof ReadFragment6b)) {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inventory:
                        showFragment(fragments.get(0), fragments.get(selectedId));
                        selectedId = 0;
                        break;
                    case R.id.navigation_bind:
                        showFragment(fragments.get(1), fragments.get(selectedId));
                        selectedId = 1;
                        break;
                    case R.id.navigation_find:
                        showFragment(fragments.get(2), fragments.get(selectedId));
                        selectedId = 2;
                        break;
                    case R.id.navigation_write:
                        client.onTag6bLog = null;
                        client.onTag6bOver = null;
                        showFragment(fragments.get(3), fragments.get(selectedId));
                        selectedId = 3;
                        break;
                    case R.id.navigation_setting:
                        SettingFragment6b settingFragment6b = (SettingFragment6b) fragments.get(4);
                        showFragment(settingFragment6b, fragments.get(selectedId));
                        settingFragment6b.initQuery();
                        selectedId = 4;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void loadGBFragments() {
        fragments.add(new ReadFragmentGB());
        fragments.add(new BindFragmentGB());
        fragments.add(new FindFragmentGB());
        fragments.add(new WriteFragmentGB());
        fragments.add(new SettingFragmentGB());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(R.id.nav_host_fragment_activity_main, fragment, fragment.getClass().getName());
            if (!(fragment instanceof ReadFragmentGB)) {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inventory:
                        showFragment(fragments.get(0), fragments.get(selectedId));
                        selectedId = 0;
                        break;
                    case R.id.navigation_bind:
                        showFragment(fragments.get(1), fragments.get(selectedId));
                        selectedId = 1;
                        break;
                    case R.id.navigation_find:
                        showFragment(fragments.get(2), fragments.get(selectedId));
                        selectedId = 2;
                        break;
                    case R.id.navigation_write:
                        client.onTagGbLog = null;
                        client.onTagGbOver = null;
                        showFragment(fragments.get(3), fragments.get(selectedId));
                        selectedId = 3;
                        break;
                    case R.id.navigation_setting:
                        SettingFragmentGB settingFragmentGb = (SettingFragmentGB) fragments.get(4);
                        showFragment(settingFragmentGb, fragments.get(selectedId));
                        settingFragmentGb.initQuery();
                        selectedId = 4;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void loadGJBFragments() {
        fragments.add(new ReadFragmentGJB());
        fragments.add(new BindFragmentGJB());
        fragments.add(new FindFragmentGJB());
        fragments.add(new WriteFragmentGJB());
        fragments.add(new SettingFragmentGJB());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(R.id.nav_host_fragment_activity_main, fragment, fragment.getClass().getName());
            if (!(fragment instanceof ReadFragmentGJB)) {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inventory:
                        showFragment(fragments.get(0), fragments.get(selectedId));
                        selectedId = 0;
                        break;
                    case R.id.navigation_bind:
                        showFragment(fragments.get(1), fragments.get(selectedId));
                        selectedId = 1;
                        break;
                    case R.id.navigation_find:
                        showFragment(fragments.get(2), fragments.get(selectedId));
                        selectedId = 2;
                        break;
                    case R.id.navigation_write:
                        client.onTagGJbLog = null;
                        client.onTagGJbOver = null;
                        showFragment(fragments.get(3), fragments.get(selectedId));
                        selectedId = 3;
                        break;
                    case R.id.navigation_setting:
                        SettingFragmentGJB settingFragmentGjb = (SettingFragmentGJB) fragments.get(4);
                        showFragment(settingFragmentGjb, fragments.get(selectedId));
                        settingFragmentGjb.initQuery();
                        selectedId = 4;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void loadTLFragments() {
        fragments.add(new ReadFragmentTL());
        fragments.add(new BindFragmentTL());
        fragments.add(new FindFragmentTL());
        fragments.add(new WriteFragmentTL());
        fragments.add(new SettingFragmentTL());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.add(R.id.nav_host_fragment_activity_main, fragment, fragment.getClass().getName());
            if (!(fragment instanceof ReadFragmentTL)) {
                transaction.hide(fragment);
            }
        }
        transaction.commitAllowingStateLoss();
        navView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_inventory:
                        showFragment(fragments.get(0), fragments.get(selectedId));
                        selectedId = 0;
                        break;
                    case R.id.navigation_bind:
                        showFragment(fragments.get(1), fragments.get(selectedId));
                        selectedId = 1;
                        break;
                    case R.id.navigation_find:
                        showFragment(fragments.get(2), fragments.get(selectedId));
                        selectedId = 2;
                        break;
                    case R.id.navigation_write:
                        client.onTagTLog = null;
                        client.onTagTLOver = null;
                        showFragment(fragments.get(3), fragments.get(selectedId));
                        selectedId = 3;
                        break;
                    case R.id.navigation_setting:
                        SettingFragmentTL settingFragmentTL = (SettingFragmentTL) fragments.get(4);
                        showFragment(settingFragmentTL, fragments.get(selectedId));
                        settingFragmentTL.initQuery();
                        selectedId = 4;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    //判断显示那个Fragment
    private void showFragment(Fragment show, Fragment hide) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (show != hide) transaction.hide(hide).show(show).commitAllowingStateLoss();
    }

    private void init() {
        PowerUtil.power("1");
        int[] baudRateList = {460800, 115200};
        for (int i : baudRateList) {
            boolean b = openReader(i + "");
            Log.e("openReader", "openReader=>" + i);
            if (b) {
                break;
            }
        }
    }


    private boolean openReader(String baudRate) {
        if (client.openAndroidSerial("/dev/ttyS3:" + baudRate, 10)) {
            MsgBaseStop stop = new MsgBaseStop();
            client.sendSynMsg(stop);
            if (stop.getRtCode() == 0) {
                return stop.getRtCode() == 0;
            } else {
                client.close();
            }
        } else {
            SmartToast.showInCenter(R.string.toast_open_serial);
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("===============", keyCode + "");
        if (selectedId == 0) {
            int tagProtocol = mmkv.decodeInt("set_tag_protocol", 0);
            switch (tagProtocol) {
                case 0:
                    ReadFragment6c readFragment6c = (ReadFragment6c) fragments.get(0);
                    readFragment6c.onKeyDown(keyCode);
                    break;
                case 1:
                    ReadFragment6b readFragment6b = (ReadFragment6b) fragments.get(0);
                    readFragment6b.onKeyDown(keyCode);
                    break;
                case 2:
                    ReadFragmentGB readFragmentGB = (ReadFragmentGB) fragments.get(0);
                    readFragmentGB.onKeyDown(keyCode);
                    break;
                case 3:
                    ReadFragmentGJB readFragmentGJB = (ReadFragmentGJB) fragments.get(0);
                    readFragmentGJB.onKeyDown(keyCode);
                    break;
                case 4:
                    ReadFragmentTL readFragmentTL = (ReadFragmentTL) fragments.get(0);
                    readFragmentTL.onKeyDown(keyCode);
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            return;
        } else {
            Toast.show(getResources().getString(R.string.back_exit));
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "main onDestroy");
//        PowerUtil.power("0");
//        client.close();


    }

}
