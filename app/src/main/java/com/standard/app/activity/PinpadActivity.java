package com.standard.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.widget.TextView;

import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.R;
import com.standard.app.card.CardManager;
import com.standard.app.PosApplication;
import com.standard.app.Utils;
import com.standard.app.cache.ConsumeData;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.util.CardSearchErrorUtil;
import com.standard.app.util.PacketProcessUtils;
import com.topwise.cloudpos.aidl.pinpad.GetPinListener;

public class PinpadActivity extends Activity {
    private static final String TAG = Utils.TAGPUBLIC + PinpadActivity.class.getSimpleName();

    //private AidlPinpad mPinpadManager;
    private AidlPinpad mPinpad;
    private int mCardType = ConsumeData.CARD_TYPE_MAG;

    private byte[] mPinBlock = null;
    private String mPinInput;

    private String mCardNo;

    private String mAmount;

    private TextView mPinTips;
    private TextView mTestCardNo;
    private TextView mTestAmount;
    private TextView mPin;
    private Intent mIntent;
    private Bundle mParam;

    private boolean mIsCancleInputKey = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad);

        //ActionBar actionBar = this.getActionBar();
        //actionBar.setTitle(R.string.title_consume);

        mIntent = getIntent();
        mParam = mIntent.getExtras();

        mAmount = PosApplication.getApp().mConsumeData.getAmount();
        mCardNo = PosApplication.getApp().mConsumeData.getCardno();
        mTestCardNo = (TextView) findViewById(R.id.card_num);
        mTestAmount = (TextView) findViewById(R.id.trad_amount);
        mPinTips = (TextView) findViewById(R.id.input_amount_tip);

        mPin = (TextView) findViewById(R.id.pin_num);
        mPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mTestCardNo.setText(getString(R.string.pin_tip_card_num) + mCardNo);
        mTestAmount.setText(getString(R.string.pin_tip_amount) + mAmount);

        //mPinpadManager = DeviceServiceManager.getInstance().getPinpadManager(0);
        mPinpad = DeviceTopUsdkServiceManager.getInstance().getPinpadManager(0);
        mCardType = PosApplication.getApp().mConsumeData.getCardType();
        CardManager.getInstance().finishPreActivity();

        showPinpadActivity(PosApplication.getApp().mConsumeData.getCardno(), PosApplication.getApp().mConsumeData.getAmount());

        CardManager.getInstance().finishPreActivity();
        CardManager.getInstance().initCardExceptionCallBack(mCallBack);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mPin.setText(mPinInput);
                    break;
                default:
                    break;
            }
        }
    };

    public void showPinpadActivity(final String cardNo, final String amount) {
        Log.i(TAG, "showPinpadActivity(), cardNo = " + cardNo);

        new Thread() {
            @Override
            public void run() {
                try {
                    mPinpad.setPinKeyboardMode(1);
                    mPinpad.getPin(getParam(cardNo, amount), mPinListener);
                    /*mPinpad.setPinKeyboardMode(1);
                    Log.d("topwise", "mPinListener: " + mPinListener);
                    mPinpad.getPin(getDukptParam(cardNo, amount), mPinListener);
                    AidlPinpad pinpad = DeviceTopUsdkServiceManager.getInstance().getPinpadManager(0);
                    Log.i(TAG, "pinpad: " + pinpad);
                    if (pinpad != null) {
                        try {
                            PosApplication.getApp().mConsumeData.setKsnValue(pinpad.getDUKPTKsn(1, false));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "pinpad.getDUKPTKsn(): " + BCDASCII.bytesToHexString(pinpad.getDUKPTKsn(1, false)));*/
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private Bundle getDukptParam(String cardNo, String amount) {
        Log.i(TAG, "getParam()");

        final Bundle param = new Bundle();
        param.putInt("wkeyid", 0x01);
        param.putInt("keytype", 0x00);
        param.putInt("key_type", 0x0d);
        param.putByteArray("random", null);
        param.putInt("inputtimes", 1);
        param.putInt("minlength", 4);
        param.putInt("maxlength", 12);
        param.putString("pan", cardNo);
        param.putString("tips", "RMB:" + amount);
        param.putBoolean("is_lkl", false);
        return param;
    }

    private Bundle getParam(String cardNo, String amount) {
        Log.i(TAG, "getParam()");
        int type = 0;
        if (mParam != null) {
            type = mParam.getInt("type", 3) == 3 ? 0 : 1;
        }

        final Bundle param = new Bundle();
        param.putInt("wkeyid", 0x00);
        param.putInt("keytype", type);
        param.putByteArray("random", null);
        param.putInt("inputtimes", 1);
        param.putInt("minlength", 4);
        param.putInt("maxlength", 12);
        param.putString("pan", cardNo);
        param.putString("tips", "RMB:" + amount);
        param.putBoolean("is_lkl", false);
        return param;
    }

    /*private GetPinListener mGetPinListener = new GetPinListener.Stub() {

        @Override
        public void onInputKey(int len, String msg) throws RemoteException {
            Log.i(TAG, "onInputKey(), len = " + len + ", msg = " + msg);

            mPinInput = msg;
            mHandler.sendEmptyMessage(1);
        }

        @Override
        public void onConfirmInput(byte[] pin) throws RemoteException {
            Log.i(TAG, "onConfirmInput(), pin = " + BCDASCII.bytesToHexString(pin));

            PosApplication.getApp().mConsumeData.setPin(pin);
            if (ConsumeData.CARD_TYPE_MAG == mCardType) {
                Intent intent = new Intent(PinpadActivity.this, PacketProcessActivity.class);
                intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
                startActivity(intent);
            } else {
                if (pin == null) {
                    CardManager.getInstance().setImportPin("000000");
                } else {
                    CardManager.getInstance().setImportPin(BCDASCII.bytesToHexString(pin));
                }
            }
        }

        @Override
        public void onCancelKeyPress() throws RemoteException {
            Log.i(TAG, "onCancelKeyPress()");
            mIsCancleInputKey = true;
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }

        @Override
        public void onStopGetPin() throws RemoteException {
            Log.i(TAG, "onStopGetPin()");
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Log.i(TAG, "onError(), errorCode = " + errorCode);
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            } else {
                showResult(getString(R.string.search_card_trans_result_stop));
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }
    };*/


    private GetPinListener mPinListener = new GetPinListener.Stub() {
        @Override
        public void onInputKey(int len, String msg) throws RemoteException {
            Log.i(TAG, "onInputKey(), len = " + len + ", msg = " + msg);

            mPinInput = msg;
            mHandler.sendEmptyMessage(1);
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
            Log.i(TAG, "onError(), errorCode = " + errorCode);
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            } else {
                showResult(getString(R.string.search_card_trans_result_stop));
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }

        @Override
        public void onConfirmInput(byte[] pin) throws RemoteException {
            Log.i(TAG, "onConfirmInput(), pin = " + BCDASCII.bytesToHexString(pin));

            boolean isOnline = false;
            if (mIntent != null) {
                Bundle bundle = mIntent.getExtras();
                if (bundle != null) {
                    isOnline = bundle.getBoolean("online");
                }
            }
            Log.d(TAG, "isOnline: " + isOnline);
            PosApplication.getApp().mConsumeData.setPin(pin);
            if (isOnline) {
                //socket通信
                Bundle bundle = new Bundle();
                bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
                CardManager.getInstance().startActivity(PinpadActivity.this, bundle, PacketProcessActivity.class);
                /*byte[] sendData = PosApplication.getApp().mConsumeData.getICData();
                Log.d(TAG, BCDASCII.bytesToHexString(sendData));
                JsonAndHttpsUtils.sendJsonData(mContext, BCDASCII.bytesToHexString(sendData));*/
            } else {
                if (ConsumeData.CARD_TYPE_MAG == mCardType) {
                    Intent intent = new Intent(PinpadActivity.this, PacketProcessActivity.class);
                    intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
                    startActivity(intent);
                } else {
                    if (pin == null) {
                        CardManager.getInstance().setImportPin("000000");
                    } else {
                        CardManager.getInstance().setImportPin(BCDASCII.bytesToHexString(pin));
                    }
                }
            }
        }

        @Override
        public void onCancelKeyPress() throws RemoteException {
            Log.i(TAG, "onCancelKeyPress()");
            mIsCancleInputKey = true;
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }

        @Override
        public void onStopGetPin() throws RemoteException {
            Log.i(TAG, "onStopGetPin()");
            if (ConsumeData.CARD_TYPE_MAG != mCardType) {
                CardManager.getInstance().setImportPin("");
            }
            CardManager.getInstance().stopCardDealService(PinpadActivity.this);
            finish();
        }
    };

    CardManager.CardExceptionCallBack mCallBack = new CardManager.CardExceptionCallBack() {
        @Override
        public void callBackTimeOut() {
            Log.i(TAG, "onDestroy()");
        }

        @Override
        public void callBackError(int errorCode) {
            Log.i(TAG, "onDestroy()");
        }

        @Override
        public void callBackCanceled() {
            Log.i(TAG, "onDestroy()");
        }

        @Override
        public void callBackTransResult(int result) {
            Log.d(TAG, "callBackTransResult result : " + result);
            if (mIsCancleInputKey) {
                return;
            }
            String resultDetail = null;
            if (result == CardSearchErrorUtil.TRANS_REASON_REJECT) {
                resultDetail = getString(R.string.search_card_trans_result_reject);
            } else if (result == CardSearchErrorUtil.TRANS_REASON_STOP) {
                resultDetail = getString(R.string.search_card_trans_result_stop);
            } else if (result == CardSearchErrorUtil.TRANS_REASON_FALLBACK) {
                resultDetail = getString(R.string.search_card_trans_result_fallback);
            } else if (result == CardSearchErrorUtil.TRANS_REASON_OTHER_UI) {
                resultDetail = getString(R.string.search_card_trans_result_other_ui);
            } else if (result == CardSearchErrorUtil.TRANS_REASON_STOP_OTHERS) {
                resultDetail = getString(R.string.search_card_trans_result_others);
            }

            showResult(resultDetail);
        }

        @Override
        public void finishPreActivity() {
            Log.i(TAG, "onDestroy()");
            PinpadActivity.this.finish();
        }
    };

    private void showResult(String detail) {
        Log.i(TAG, "showResult(), detail = " + detail);
        Intent intent = new Intent(this, ShowResultActivity.class);
        intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
        intent.putExtra("result_resDetail", detail);
        startActivity(intent);
        this.finish();
    }
}