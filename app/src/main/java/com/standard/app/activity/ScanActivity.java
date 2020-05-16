package com.standard.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.qrcode.QrCodeActivity;
import com.standard.app.util.PacketProcessUtils;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCodeListener;

import java.util.List;

public class ScanActivity {
    private static final String TAG = Utils.TAGPUBLIC + ScanActivity.class.getSimpleName();

    /*private Context mContext;
    private Bundle mBundle;
    //private AidlFacingbackCamera ScanManager;

    public void scanActivityShow(Context context, Bundle data) {
        Log.i(TAG, "scanActivityShow()");
        mContext = context;
        mBundle = data;
        //ScanManager = DeviceServiceManager.getInstance().getScanManager();

        onScannerClick(context);
    }

    public void onScannerClick(final Context context){
        Log.i(TAG, "onScannerClick()");
        /*Log.i(TAG, "ScanManager == null : "+(ScanManager == null));
        try {
            ScanManager.open();
            ScanManager.scanCode(mAidlCameraScanCodeListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*private AidlCameraScanCodeListener mAidlCameraScanCodeListener = new AidlCameraScanCodeListener.Stub(){
        @Override
        public void onSuccess(String code) throws RemoteException {
            PosApplication.getApp().mConsumeData.setScanResult(code);

            Intent intent = new Intent(mContext, PacketProcessActivity.class);
            intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_SCAN);
            mContext.startActivity(intent);
        }

        @Override
        public void onFail(int errorCode) throws RemoteException {
            Log.i(TAG, "onFail(), errorCode = "+errorCode);
            Toast.makeText(mContext, mContext.getString(R.string.scan_error_code), Toast.LENGTH_SHORT).show();
        }
    };*/
    private static final String RESULT_DETAIL = "result_resDetail";

    private Context mContext;

    public void scanActivityShow(Context context) {
        Log.d(TAG,"");
        mContext = context;

        onScannerClick();
    }

    public void onScannerClick() {

        boolean checkKey = checkCameraHardWare(mContext);
        Log.d(TAG,"checkKey:" + checkKey);
        if (mAidlCameraScanCodeListener == null) {
            Log.d(TAG,"mAidlCameraScanCodeListener = null error");
        }
        QrCodeActivity.setAidlCameraScanCodeListener(mAidlCameraScanCodeListener);
        Intent intent = new Intent(mContext, QrCodeActivity.class);
        String amount = PosApplication.getApp().mConsumeData.getAmount();
        intent.putExtra("amount", amount);
        mContext.startActivity(intent);
    }

    private AidlCameraScanCodeListener mAidlCameraScanCodeListener = new AidlCameraScanCodeListener.Stub() {
        @Override
        public void onResult(String code) throws RemoteException {
            Log.d(TAG,"scan Code = " + code);
            PosApplication.getApp().mConsumeData.setScanResult(code);

            Intent intent = new Intent(mContext, PacketProcessActivity.class);
            intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_SCAN);
            mContext.startActivity(intent);
        }

        @Override
        public void onCancel() throws RemoteException {
            Log.d(TAG,"scan onCancel");
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Log.d(TAG,"scan onFail(), errorCode = " + errorCode);
            Toast.makeText(mContext, mContext.getString(R.string.scan_error_code) + errorCode, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onTimeout() throws RemoteException {
            Log.d(TAG,"scan onTimeout");
        }

    };

    private boolean checkCameraHardWare(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}