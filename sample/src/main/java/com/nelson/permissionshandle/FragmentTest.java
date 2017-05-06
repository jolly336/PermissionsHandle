package com.nelson.permissionshandle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.nelson.annotation.PermissionDenied;
import com.nelson.annotation.PermissionGrant;
import com.nelson.api.PermissionsHandle;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nelson.permissionshandle.RequestCodeConstants.REQUEST_CODE_CONTRACT;
import static com.nelson.permissionshandle.RequestCodeConstants.REQUEST_CODE_OPEN_CAMERA;

/**
 * A simple {@link Fragment} subclass.
 * Created by Nelson on 17/5/6.
 */

public class FragmentTest extends Fragment {

    @Bind(R.id.btn_camera)
    Button mBtnOpenCamera;
    @Bind(R.id.btn_contract)
    Button mBtnContract;

    private Activity mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @OnClick({R.id.btn_camera, R.id.btn_contract})
    public void open(View view) {
        if (view.equals(mBtnOpenCamera)) {
            PermissionsHandle.requestPermissions(this, REQUEST_CODE_OPEN_CAMERA, Manifest.permission.CAMERA);
        } else if (view.equals(mBtnContract)) {
            PermissionsHandle.requestPermissions(this,
                    REQUEST_CODE_CONTRACT,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_PHONE_STATE);
        }
    }

    //--------open camera----------------
    @PermissionGrant(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraSucess() {
        Toast.makeText(mContext, "Grant app access camera!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_OPEN_CAMERA)
    public void requestCameraFailed() {
        Toast.makeText(mContext, "Deny app access camera!!!", Toast.LENGTH_SHORT).show();
    }

    //--------contract ---------------
    @PermissionGrant(REQUEST_CODE_CONTRACT)
    public void requestContractSucess() {
        Toast.makeText(mContext, "Grant app access contract!", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(REQUEST_CODE_CONTRACT)
    public void requestContractFailed() {
        Toast.makeText(mContext, "Deny app access contract!!!", Toast.LENGTH_SHORT).show();
    }

    //--------request result----------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsHandle.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    public static Fragment newInstance() {
        return new FragmentTest();
    }
}
