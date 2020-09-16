package com.example.capstoneproject.utils;

import android.content.Context;

public class Utils {
    public static int convertDpToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
