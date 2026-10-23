package com.pda.uhfdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.protocol.gx.MsgAppReset;
import com.gg.reader.api.protocol.gx.MsgUpgradeBaseband;
import com.gg.reader.api.utils.ThreadPoolUtils;
import com.pda.uhfdemo.utils.GlobalClient;
import com.pda.uhfdemo.utils.Toast;
import com.pda.uhfdemo.utils.UriUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class UpgradeActivity extends AppCompatActivity {

    @BindView(R.id.bin_file_path)
    EditText bin_file_path;
    @BindView(R.id.upgradeBar)
    NumberProgressBar numberProgressBar;
    @BindView(R.id.select_bin_file)
    Button select_bin_file;
    @BindView(R.id.upgrade_btn)
    Button upgrade_btn;
    boolean upRunning;
    GClient client = GlobalClient.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        ButterKnife.bind(this);
    }


    int REQUEST_PERMISSION_CODE = 0xff;
    @OnClick(R.id.select_bin_file)
    public void selectBinFileEvent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
//                Log.i("requestPermission", "isExternalStorageManager");
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                return;
            }
        }
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, getResources().getString(R.string.upBase_permission_name), me.weyye.hipermission.R.drawable.permission_ic_storage));
        HiPermission.create(getBaseContext())
                .title(getResources().getString(R.string.upBase_permission_title))
                .permissions(permissionItems)
                .msg(getResources().getString(R.string.upBase_permission_msg))
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onFinish() {
                        bin_file_path.setText(null);
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, 1);
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            Log.i("requestPermission", "request permission success");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R.id.upgrade_btn)
    public void upgradeEvent() {
        if (bin_file_path.getText().toString().equals("")) {
            Toast.show(getResources().getString(R.string.select_upgrade));
        } else {
            if (!upRunning) {
                numberProgressBar.setProgress(0);
                ThreadPoolUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        upgradeBase();
                    }
                });
            } else {
                Toast.show(getResources().getString(R.string.upgrading));
            }
        }
    }

    private void upgradeBase() {
        try {
            upRunning = true;
            File file = new File(bin_file_path.getText().toString());
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            long totalCount = file.length();
            byte[] buffer = new byte[256];
            int complete = 0;
            long count = 1L;
            int len = -1;
            MsgUpgradeBaseband msg = new MsgUpgradeBaseband();
            msg.setPacketNumber(0L);
            client.sendSynMsg(msg);
            if (msg.getRtCode() != 0) {
                client.sendSynMsg(msg);
            }
            if (msg.getRtCode() != 0) {
                upRunning = false;
                upHandler.sendEmptyMessage(3);
                return;
            }
            while ((len = bis.read(buffer)) != -1) {
                msg.setPacketNumber(count);
                msg.setPacketContent(buffer);
                client.sendSynMsg(msg);
                if (msg.getRtCode() != 0 || msg.getPacketNumber() != count) {
                    upHandler.sendEmptyMessage(2);
                    msg.setRtCode((byte) -2);
                    break;
                }
                msg.setRtCode((byte) -1);
                count++;
                complete += len;
                int rate = (int) ((float) complete / (float) totalCount * 100);
                Message message = new Message();
                message.what = 1;
                message.arg1 = rate;
                upHandler.sendMessage(message);
//                Thread.sleep(300);
            }
            if (msg.getRtCode() != -2) {
                msg.setPacketNumber(Long.parseLong("FFFFFFFF", 16));
                msg.setPacketContent((byte[]) null);
                client.sendSynMsg(msg);
                if (msg.getRtCode() != 0) {
                    upHandler.sendEmptyMessage(4);
                } else {
                    upHandler.sendEmptyMessage(5);
                    Thread.sleep(2000L);
                    client.sendSynMsg(new MsgAppReset());
                }
            }
            bis.close();
            upRunning = false;
        } catch (Exception e) {
            Log.e("upgrade", e.toString());
            upRunning = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.show(e.toString());
                }
            });
        }
    }


    private Handler upHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    numberProgressBar.setProgress(msg.arg1);
                    break;
                case 2:
                    Toast.show(getString(R.string.upgrade_hint2));
                    break;
                case 3:
                    Toast.show(getString(R.string.upgrade_hint3));
                    break;
                case 4:
                    Toast.show(getString(R.string.upgrade_hint4));
                    break;
                case 5:
                    Toast.show(getString(R.string.upgrade_hint5));
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri.getPath().lastIndexOf(".bin") != -1) {
                            String path = UriUtils.getPath(this, uri);
                            bin_file_path.setText(path);
                        } else {
                            Toast.show(getString(R.string.upgrade_hint1));
                        }
                    }
                }
                break;
        }
    }
}