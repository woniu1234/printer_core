package com.lst.printerlib.utils;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author lst
 * @Description: ToastUtil.java(类描述)
 * @date 2021/6/22
 */
public class PrintToastUtils {
    private static Toast mToast;

    public static void show(String msg, Context context) {
        try {
            if (!TextUtils.isEmpty(msg)) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
                mToast.setText(msg);
                mToast.show();
            }
        } catch (Exception e) {
            Looper.prepare();
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
            mToast.setText(msg);
            mToast.show();
            Looper.loop();
        }
    }
}
