package com.nelson.permissionshandle;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.nelson.annotation.PermissionDenied;
import com.nelson.annotation.PermissionGrant;
import com.nelson.annotation.ShowRequestPermissionRationale;
import com.nelson.api.PermissionsHandle;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SDCARD = 2;
    private static final int REQUEST_CODE_CALL_PHONE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void doTestSdcard(View view) {
        if (!PermissionsHandle.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_SDCARD)) {
            PermissionsHandle.requestPermissions(this, REQUEST_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


    public void doTestCallPhone(View view) {
        PermissionsHandle.requestPermissions(this, REQUEST_CODE_CALL_PHONE, Manifest.permission.CALL_PHONE);
    }

    @ShowRequestPermissionRationale(REQUEST_CODE_SDCARD)
    public void whyNeedSdcard() {
        Toast.makeText(this, "I need write something to sdcard,please ensure you open this!", Toast.LENGTH_SHORT).show();
        PermissionsHandle.requestPermissions(this, REQUEST_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @PermissionGrant(REQUEST_CODE_SDCARD)
    public void requestSdcardSucess() {
        Toast.makeText(this, "Grant app access Sdcard!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_SDCARD)
    public void requestSdcardFailed() {
        Toast.makeText(this, "Deny app access Sdcard!!!", Toast.LENGTH_SHORT).show();
    }

    @PermissionGrant(REQUEST_CODE_CALL_PHONE)
    public void requestCallPhoneSucess() {
        Toast.makeText(this, "Grant app access call phone!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_CALL_PHONE)
    public void requestCallPhoneFailed() {
        Toast.makeText(this, "Deny app access call phone!!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsHandle.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
