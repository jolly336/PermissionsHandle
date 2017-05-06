package com.nelson.permissionshandle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Nelson on 17/5/6.
 */

public class FragmentTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_container, FragmentTest.newInstance())
                .commitAllowingStateLoss();
    }
}
