package com.standard.app.packet;

import android.util.Log;

import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;

public class UnpackEmv {
    private static final String TAG = UnpackEmv.class.getSimpleName();

    private String resMsg = null;
    private String resDetail = null;
    private byte[] mField62 = null;

    public UnpackEmv(byte[] srcData,int srcDataLen) {
        Log.d(TAG, "UnPackEmvCapkOrPara ... ");

        UnpackUtils unpack = new UnpackUtils();
        ISO8583 mIso = unpack.UnpackFront(srcData, srcDataLen);
        if(mIso == null)
            return;

        resMsg = new String(mIso.getBit(39));
        Log.d(TAG,"field 39 is：" + resMsg);
        resDetail = unpack.processField46(mIso.getBit(46), resMsg);

        if (mIso.getBit(62) != null) {
            mField62 = mIso.getBit(62);
        }
        Log.d(TAG,"unpack field62:  " + BCDASCII.bytesToHexString(mField62));
    }

    public String getResponse() {
        return resMsg;
    }

    public String getResponseDetail() {
        Log.i(TAG, "resDetail = "+resDetail);
        return resDetail;
    }

    public byte[] getField62() {
        if (mField62 == null) {
            return null;
        }
        byte[] f62 = new byte[mField62.length-1];
        System.arraycopy(mField62, 1, f62, 0, mField62.length-1);
        return f62;
    }

    public int getField62FirstByte() {
        return (mField62[0] & 0xFF);
    }
}
