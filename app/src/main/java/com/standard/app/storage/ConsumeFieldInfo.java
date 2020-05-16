package com.standard.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.standard.app.Utils;

public class ConsumeFieldInfo {
    private static final String TAG = Utils.TAGPUBLIC + ConsumeFieldInfo.class.getSimpleName();

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public ConsumeFieldInfo(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences("consume_field_value", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void setIsNeedPositiveValue(String key, boolean isNeedPositive) {
        Log.i(TAG, "setIsNeedPositiveValue, key = "+key+", isNeedPositive = "+isNeedPositive);
        mEditor.putBoolean(key, isNeedPositive);
        mEditor.commit();
    }

    public boolean getIsNeedPositiveValue(String key) {
        Log.i(TAG, "getIsNeedPositiveValue, key = "+key);
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setField(String key, String value) {
        Log.i(TAG, "setField, key = "+key+", value = "+value);
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getField(String key) {
        Log.i(TAG, "getField  "+mSharedPreferences.getString(key, null));
        return mSharedPreferences.getString(key, null);
    }

    public void clearField() {
        mEditor.clear().commit();
    }

    public void setInit(boolean isInit) {
        mEditor.putBoolean("IS_INIT", isInit);
        mEditor.commit();
    }

    public boolean getInit() {
        return mSharedPreferences.getBoolean("IS_INIT", false);
    }
}