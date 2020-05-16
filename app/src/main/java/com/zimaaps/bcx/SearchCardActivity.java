package com.zimaaps.bcx;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.activity.ShowResultActivity;
import com.standard.app.cache.ConsumeData;
import com.standard.app.card.CardManager;
import com.standard.app.card.CheckCardListenerSub;
import com.standard.app.util.CardSearchErrorUtil;
import com.standard.app.util.PacketProcessUtils;

public class SearchCardActivity extends Activity{
    private static final String TAG = Utils.TAGPUBLIC + SearchCardActivity.class.getSimpleName();

    private static final int MSG_TIME_UPDATE = 100;

    private TextView mTextAmount;
    private TextView mTextTime;
    private String mAmount;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() SEARCH CARD ACTIVITY STARTED");
        setContentView(R.layout.activity_search_card);

        //ActionBar actionBar = this.getActionBar();
        //actionBar.setTitle(R.string.title_consume);

        mTextAmount = (TextView) findViewById(R.id.trad_amount);
        mTextTime = (TextView) findViewById(R.id.text_time);

        mAmount = PosApplication.getApp().mConsumeData.getAmount();
        mTextAmount.setText("Tsh "+mAmount);

        CardManager.getInstance().startCardDealService(this);
        CardManager.getInstance().initCardExceptionCallBack(exceptionCallBack);
        //CardManager.getInstance().initCardResultCallBack(cardResultCallBack);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @SuppressLint("HandlerLeak")
    Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage what : " + msg.what);
            switch (msg.what) {
                case CardSearchErrorUtil.CARD_SEARCH_ERROR_REASON_MAG_READ:
                    showTips(R.string.search_card_error_msr_read);
                    break;
                case CardSearchErrorUtil.CARD_SEARCH_ERROR_REASON_MAG_EMV:
                    showTips(R.string.search_card_error_msr_is_ic);
                    break;
                case CardSearchErrorUtil.CARD_SEARCH_ERROR_REASON_MAG_EMV_s:
                    showTips(R.string.search_card_error_msr_is_ic);
                    break;
                default:
                    break;
            }
            CardManager.getInstance().startCardDealService(SearchCardActivity.this);
        }
    };

    private void showTips(int resId) {
        if (null != mToast) {
            mToast.cancel();
        }
        mToast = Toast.makeText(SearchCardActivity.this, getString(resId), Toast.LENGTH_SHORT);
        mToast.show();
    }

    private CardManager.CardExceptionCallBack exceptionCallBack = new CardManager.CardExceptionCallBack() {
        @Override
        public void callBackTimeOut() {
            Log.i(TAG, "callBackTimeOut");
            SearchCardActivity.this.finish();
        }

        @Override
        public void callBackError(int errorCode) {
            Log.i(TAG, "callBackError errorCode : " + errorCode);
            mHandle.sendEmptyMessage(errorCode);
        }


        @Override
        public void callBackCanceled() {
            Log.i(TAG, "callBackCanceled");
        }

        @Override
        public void callBackTransResult(int result) {
            Log.i(TAG, "callBackTransResult result : " + result);
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
            Log.i(TAG, "finishPreActivity");
            SearchCardActivity.this.finish();
        }
    };

    private void showResult(String detail) {
        Log.i(TAG, "showResult(), detail = "+detail);




/*
        Intent intent = new Intent(this, ShowResultActivity.class);
        intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
        intent.putExtra("result_resDetail", detail);
        startActivity(intent);
        this.finish();
*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed");
        CardManager.getInstance().stopCardDealService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }







    private CardManager.CardResultCallBack cardResultCallBack = new CardManager.CardResultCallBack() {
        @Override
        public void consumeAmount(String amount) {

        }

        @Override
        public void aidSelect(int index) {

        }

        @Override
        public void eCashTipsConfirm(boolean confirm) {

        }

        @Override
        public void confirmCardInfo(boolean confirm) {

        }

        @Override
        public void importPin(String pin) {

        }

        @Override
        public void userAuth(boolean auth) {

        }

        @Override
        public void requestOnline(boolean online, String respCode, String icc55) {
            Log.i("SEARCH CARD ACTIVITY", "RESPONCE CODE "+respCode);
        }

    };





}
