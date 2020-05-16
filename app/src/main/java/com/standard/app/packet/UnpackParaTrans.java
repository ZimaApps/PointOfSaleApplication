package com.standard.app.packet;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.storage.MerchantInfo;

public class UnpackParaTrans {
    private static final String TAG = Utils.TAGPUBLIC + UnpackParaTrans.class.getSimpleName();

    private String resMsg = null;
    private String resDetail = null;

    public UnpackParaTrans(Context context, byte[] srcData, int srcDataLen) {
        Log.d(TAG, "UnPackParamTrans ... ");

        UnpackUtils unpack = new UnpackUtils();
        ISO8583 mIso = unpack.UnpackFront(srcData, srcDataLen);
        if(mIso == null)
            return;

        resMsg = new String(mIso.getBit(39));
        Log.d(TAG,"field 39 is：" + resMsg);
//        resDetail = unpack.processField46(app, mIso.getBit(46), resMsg);

        if (resMsg.equals("00")) {
            //解析62域
            byte[] ddata = new byte[64];
            System.arraycopy(mIso.getBit(62), 0, ddata, 0, 4);
            Log.d(TAG, new String(ddata, 0, 4));

            System.arraycopy(mIso.getBit(62), 4, ddata, 0, 4);
            Log.d(TAG, new String(ddata, 2, 2));
            Log.d(TAG, new String(ddata, 2, 2));

            System.arraycopy(mIso.getBit(62), 8, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 2, 1));
            Log.d(TAG, new String(ddata, 2, 1));

            System.arraycopy(mIso.getBit(62), 11, ddata, 0, 16);
            Log.d(TAG, new String(ddata, 2, 14));
            Log.d(TAG, new String(ddata, 2, 14));

            System.arraycopy(mIso.getBit(62), 27, ddata, 0, 16);
            Log.d(TAG, new String(ddata, 2, 14));
            Log.d(TAG, new String(ddata, 2, 14));

            System.arraycopy(mIso.getBit(62), 43, ddata, 0, 16);
            Log.d(TAG, new String(ddata, 2, 14));
            Log.d(TAG, new String(ddata, 2, 14));

            System.arraycopy(mIso.getBit(62), 59, ddata, 0, 16);
            Log.d(TAG, new String(ddata, 2, 14));
            Log.d(TAG, new String(ddata, 2, 14));

            System.arraycopy(mIso.getBit(62), 75, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 2, 1));
//            if ("1".equals(new String(ddata, 2, 1)))
//                app.mTran.setSupportTip(true);
//            else
//                app.mTran.setSupportTip(false);

            System.arraycopy(mIso.getBit(62), 78, ddata, 0, 4);
            Log.d(TAG, new String(ddata, 0, 4));
            Log.d(TAG, new String(ddata, 2, 2).trim());

            System.arraycopy(mIso.getBit(62), 82, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));
//            if ("1".equals(new String(ddata, 2, 1)))
//                app.mTerm.setAllInputCardNum(true);
//            else
//                app.mTerm.setAllInputCardNum(false);

            System.arraycopy(mIso.getBit(62), 85, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));
//            if ("1".equals(new String(ddata, 2, 1)))
//                app.mTran.setSupportAutoLogoutAfterSettle(true);
//            else
//                app.mTran.setSupportAutoLogoutAfterSettle(false);

            System.arraycopy(mIso.getBit(62), 88, ddata, 0, 42);

            byte[] mn = new byte[40];
            System.arraycopy(ddata, 2, mn, 0, 40);

            try {
                Log.d(TAG, new String(ddata, 2, 40, "GBK").trim());
                MerchantInfo merchantInfo = new MerchantInfo(context);
                merchantInfo.setMerchantName(new String(ddata, 2, 40, "GBK").trim());
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            System.arraycopy(mIso.getBit(62), 130, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));
            Log.d(TAG, new String(ddata, 2, 1));

            //离线上送方式
            System.arraycopy(mIso.getBit(62), 133, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));

            //撤销交易控制信息，刷卡田输密之类
            System.arraycopy(mIso.getBit(62), 136, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 4));

            //支持的交易类型	交易屏蔽
            System.arraycopy(mIso.getBit(62), 139, ddata, 0, 6);
            Log.d(TAG, new String(ddata, 0, 4));

            System.arraycopy(mIso.getBit(62), 145, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));

            //是否屏蔽卡号
            System.arraycopy(mIso.getBit(62), 148, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));

            //是否显示Logo
            System.arraycopy(mIso.getBit(62), 151, ddata, 0, 3);
            Log.d(TAG, new String(ddata, 0, 3));

            System.arraycopy(mIso.getBit(62), 154, ddata, 0, 4);
            Log.d(TAG, new String(ddata, 0, 4));
        }
    }

    public String getResponse() {
        return resMsg;
    }

    public String getResponseDetail() {
        return resDetail;
    }
}
