package com.standard.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.standard.app.card.CardManager;
import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.cache.ConsumeData;
import com.standard.app.util.CardSearchErrorUtil;
import com.standard.app.util.PacketProcessUtils;

public class CardConfirmActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + CardConfirmActivity.class;

    private TextView mTextCardNo;
    private String mCardNo;
    private String mAmount;
    private int mCardType = ConsumeData.CARD_TYPE_MAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        Log.i(TAG, "CARD COMFIRM HAS BEEN CALLED = ");

        setContentView(R.layout.activity_card_confirm);

        //ActionBar actionBar = this.getActionBar();
        //actionBar.setTitle(R.string.title_consume);

        mAmount = PosApplication.getApp().mConsumeData.getAmount();
        mCardNo = PosApplication.getApp().mConsumeData.getCardno();

        mTextCardNo = (TextView) findViewById(R.id.card_num);
        mTextCardNo.setText(mCardNo);
        mCardType = PosApplication.getApp().mConsumeData.getCardType();
        CardManager.getInstance().finishPreActivity();
        CardManager.getInstance().initCardExceptionCallBack(mCallBack);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancle:
                if (ConsumeData.CARD_TYPE_MAG != mCardType)
                    CardManager.getInstance().setConfirmCardInfo(false);
                finish();
                break;
            case R.id.btn_ok:
                if (ConsumeData.CARD_TYPE_MAG == mCardType) {
                    Intent intent = new Intent(this, PinpadActivity.class);
                    startActivity(intent);
                } else {
                    CardManager.getInstance().setConfirmCardInfo(true);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (ConsumeData.CARD_TYPE_MAG != mCardType) {
            CardManager.getInstance().setConfirmCardInfo(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

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
            Log.i(TAG, "onDestroy()");
            Log.d(TAG, "callBackTransResult result : " + result);
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
            CardConfirmActivity.this.finish();
        }
    };

    private void showResult(String detail) {
        Log.i(TAG, "showResult(), detail = "+detail);
        Intent intent = new Intent(this, ShowResultActivity.class);
        intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
        intent.putExtra("result_resDetail", detail);
        startActivity(intent);
        this.finish();
    }
}
