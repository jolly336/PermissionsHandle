package com.nelson.permissionshandle;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nelson.annotation.PermissionDenied;
import com.nelson.annotation.PermissionGrant;
import com.nelson.annotation.ShowRequestPermissionRationale;
import com.nelson.api.PermissionsHandle;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nelson.permissionshandle.RequestCodeConstants.REQUEST_CODE_CALL_PHONE;
import static com.nelson.permissionshandle.RequestCodeConstants.REQUEST_CODE_OPEN_CAMERA;
import static com.nelson.permissionshandle.RequestCodeConstants.REQUEST_CODE_SDCARD;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_sdcard)
    Button mBtnSdcard;
    @Bind(R.id.btn_call)
    Button mBtnCallPhone;
    @Bind(R.id.btn_open_camera)
    Button mBtnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @OnClick({R.id.btn_sdcard, R.id.btn_call, R.id.btn_open_camera})
    public void open(View view) {
        switch (view.getId()) {
            case R.id.btn_sdcard:
                if (!PermissionsHandle.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_SDCARD)) {
                    PermissionsHandle.requestPermissions(this, REQUEST_CODE_SDCARD, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.btn_call:
                PermissionsHandle.requestPermissions(this, REQUEST_CODE_CALL_PHONE, Manifest.permission.CALL_PHONE);
                break;
            case R.id.btn_open_camera:
                if (!PermissionsHandle.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA, REQUEST_CODE_OPEN_CAMERA)) {
                    PermissionsHandle.requestPermissions(this, REQUEST_CODE_OPEN_CAMERA, Manifest.permission.CAMERA);
                }
                break;
        }
    }


    //--------W/R sdcard----------------
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


    //--------call phone----------------
    @PermissionGrant(REQUEST_CODE_CALL_PHONE)
    public void requestCallPhoneSucess() {
        Toast.makeText(this, "Grant app access call phone!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_CALL_PHONE)
    public void requestCallPhoneFailed() {
        Toast.makeText(this, "Deny app access call phone!!!", Toast.LENGTH_SHORT).show();
    }


    //--------open camera----------------
    @PermissionGrant(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraSucess() {
        Toast.makeText(this, "Grant app access camera!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraFailed() {
        Toast.makeText(this, "Deny app access camera!!!", Toast.LENGTH_SHORT).show();
    }

    @ShowRequestPermissionRationale(REQUEST_CODE_OPEN_CAMERA)
    public void whyNeedCamera() {
        Toast.makeText(this, "I need use camera,please ensure you open this!", Toast.LENGTH_SHORT).show();
        PermissionsHandle.requestPermissions(this, REQUEST_CODE_OPEN_CAMERA, Manifest.permission.CAMERA);
    }

    //--------request result----------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsHandle.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_fragment) {
            startActivity(new Intent(this, FragmentTestActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
