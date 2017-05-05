package com.nelson.api;

import android.os.Build;

/**
 * Created by Nelson on 17/5/5.
 */

public final class Utils {

    private static boolean overMarshmallow;

    private Utils() {
    }


    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
