package com.zimaaps.bcx;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.cache.ConsumeData;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.settings.SettingsActivity;
import com.standard.app.settings.TradManagerSettingActivity;
import com.standard.app.storage.ConsumeFieldInfo;
import com.topwise.cloudpos.aidl.emv.AidlPboc;
import com.topwise.cloudpos.aidl.led.AidlLed;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.data.PinpadConstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//cmp=com.centerm.frame/.Detect

public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + MainActivity.class.getSimpleName();

    private static final int DIALOG_EXIT_APP = 100;

    private AlertDialog.Builder mAlerDialog;

    public static ConsumeFieldInfo info;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_select_flow);
        setContentView(R.layout.main_layout);
        PosApplication.getApp().getDeviceManager();
        info =new ConsumeFieldInfo(this);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Init Application");
        mProgressDialog.show();
        initApp();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle data = new Bundle();
        switch (v.getId()) {
            case R.id.normal_flow:
                PosApplication.getApp().setConsumeData();
                PosApplication.getApp().mConsumeData.setConsumeType(ConsumeData.CONSUME_TYPE_CARD);
                intent.setClass(this, AmountInputActivity.class);
                intent.putExtras(data);
                startActivity(intent);
                break;
            case R.id.scan_pay_flow:
                Log.i(TAG, "scan_pay_flow");
                PosApplication.getApp().setConsumeData();
                PosApplication.getApp().mConsumeData.setConsumeType(ConsumeData.CONSUME_TYPE_SCAN);
                intent.setClass(this, AmountInputActivity.class);
                intent.putExtras(data);
                startActivity(intent);
                break;

            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        showDialogExt(DIALOG_EXIT_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
      /*  IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);*/

        AidlLed mAidlLed = DeviceTopUsdkServiceManager.getInstance().getLedManager();
        try {
            if(mAidlLed != null){
                mAidlLed.setLed(0 , false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showDialogExt(int type) {
        switch (type) {
            case DIALOG_EXIT_APP:
                mAlerDialog = new AlertDialog.Builder(this);
                mAlerDialog.setCancelable(false);
                mAlerDialog.setMessage(getString(R.string.is_exit_app));
                mAlerDialog.setNegativeButton(R.string.dialog_cancle, null);
                mAlerDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                mAlerDialog.show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        //      unregisterReceiver(mCloseSystemDialogsReceiver);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        Log.i(TAG, "event " +event.getKeyCode());

        return super.dispatchKeyEvent(event);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initApp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                downLoadParM();
                downLoadKeys();
                info.setInit(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mProgressDialog!= null && mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                        }

                    }
                });
            }
        }).start();

    }


    private void downLoadParM(){
        AidlPboc mPbocManager = DeviceTopUsdkServiceManager.getInstance().getPbocManager();
        try {
            //读取assert下的IC卡参数配置文件，将相关参数加载到EMV内核
            try {
                boolean updateResult = false;
                boolean flag = true;
                int i = 0;
                String success = "";
                String fail = "";
                // 获取IC卡参数信息
                mPbocManager.updateAID(0x03, null);
                mPbocManager.updateCAPK(0x03, null);

                InputStream ins = this.getAssets().open("icparam/ic_param.txt");
                if (ins != null && ins.available() != 0x00) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(ins));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        // 未到达文件末尾
                        if (null != line) {
                            if (line.startsWith("AID")) {
                                // 更新AID
                                updateResult = mPbocManager.updateAID(0x01, line.split("=")[1]);

                            } else { // 更新RID
                                updateResult = mPbocManager.updateCAPK(0x01, line.split("=")[1]);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }


    private void downLoadKeys(){

        final AidlPinpad pinpadManager = DeviceTopUsdkServiceManager.getInstance().getPinpadManager(0);
        final byte[] tmk = BCDASCII.hexStringToBytes("89F8B0FDA2F2896B9801F131D32F986D89F8B0FDA2F2896B");
        final byte[] tak = BCDASCII.hexStringToBytes("92B1754D6634EB22");
        final byte[] tpk = BCDASCII.hexStringToBytes("B5E175AC5FD8DD8A03AD23A35C5BAB6B");
        final byte[] trk = BCDASCII.hexStringToBytes("744185122EEC284830694CAD383B4F7A");
        boolean mIsSuccess =false;

        try {
            mIsSuccess = pinpadManager.loadMainkey(0, tmk, null);

            mIsSuccess = pinpadManager.loadWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_MAK, 0, 0, tak, null);

            mIsSuccess = pinpadManager.loadWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_PIK, 0, 0, tpk, null);

            mIsSuccess = pinpadManager.loadWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_TDK, 0, 0, trk, null);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

}

