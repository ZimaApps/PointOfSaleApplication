package com.standard.app.util;

import android.util.Log;

import com.standard.app.PosApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author xukun
 * @version 1.0.0
 * @date 18-6-12
 */

public class ExtraUtil {

    public static String getCustomVersionMsg(String originalMsg) {
        StringBuilder version = new StringBuilder();
        if (originalMsg != null) {
            version.append(originalMsg);
            version.append("-");
        }
        InputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = PosApplication.getApp().getAssets().open("version.ver");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            if ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        version.append(content.toString());
        Log.i("topwise","getCustomVersionMsg: " + version.toString());
        return version.toString();
    }
}
