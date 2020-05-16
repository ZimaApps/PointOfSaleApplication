package com.standard.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.standard.app.Utils;

public class TradManagerInfo {
    private static final String TAG = Utils.TAGPUBLIC + TradManagerInfo.class.getSimpleName();

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public TradManagerInfo(Context context) {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences("trad_manager_info", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public void setIsRfNoPin(boolean isRfNoPin) {
        Log.i(TAG, "setIsRfNoPin  "+isRfNoPin);
        mEditor.putBoolean(TradManagerUtils.IS_RF_NO_PIN, isRfNoPin);
        mEditor.commit();
    }

    public boolean getIsRfNoPin() {
        Log.i(TAG, "getIsRfNoPin  "+mSharedPreferences.getBoolean(TradManagerUtils.IS_RF_NO_PIN, false));
        return mSharedPreferences.getBoolean(TradManagerUtils.IS_RF_NO_PIN, TradManagerUtils.rf_no_pin_def);
    }

    public void setNoPinAmt(String noPinAmt) {
        Log.i(TAG, "setNoPinAmt  "+noPinAmt);
        mEditor.putString(TradManagerUtils.NO_PIN_AMOUNT, noPinAmt);
        mEditor.commit();
    }

    public String getNoPinAmt() {
        Log.i(TAG, "getNoPinAmt  "+mSharedPreferences.getString(TradManagerUtils.NO_PIN_AMOUNT, null));
        return mSharedPreferences.getString(TradManagerUtils.NO_PIN_AMOUNT, TradManagerUtils.rf_no_pin_max_amount_def);
    }

    public void setIsNoSign(boolean isNoSign) {
        Log.i(TAG, "setIsNoSign  "+isNoSign);
        mEditor.putBoolean(TradManagerUtils.IS_NO_SIGN, isNoSign);
        mEditor.commit();
    }

    public boolean getIsNoSign() {
        Log.i(TAG, "getIsNoSign  "+mSharedPreferences.getBoolean(TradManagerUtils.IS_NO_SIGN, false));
        return mSharedPreferences.getBoolean(TradManagerUtils.IS_NO_SIGN, TradManagerUtils.no_sign_def);
    }
}