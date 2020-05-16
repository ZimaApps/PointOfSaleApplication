package com.zimaaps.bcx;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.storage.ConsumeFieldInfo;
import com.standard.app.util.PacketProcessUtils;
import com.topwise.cloudpos.aidl.led.AidlLed;

public class ShowResultActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + ShowResultActivity.class.getSimpleName();

    private ImageView mImageResult;
    private TextView mTextResponse;
    private TextView mTextResDetail;

    private int mProcType;
    public int mErrorReson = 0;
    private String mResponse;
    private String mResponseDetail;

    private AidlLed mAidlLed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_show_result);

        mImageResult = (ImageView) findViewById(R.id.image_result);
        mTextResponse = (TextView) findViewById(R.id.result_response);
        mTextResDetail = (TextView) findViewById(R.id.result_detail);


        Bundle data = getIntent().getExtras();
        mProcType = data.getInt(PacketProcessUtils.PACKET_PROCESS_TYPE);
        mResponse = data.getString("result_response");
        mResponseDetail = data.getString("result_resDetail");
        mErrorReson = data.getInt("result_errReason");
        Log.i(TAG, "mResponse = " + mResponse + ", mResponseDetail" + mResponseDetail + ", mErrorReson" + mErrorReson);

        setActionBarText();

        showResult();
    }

    private void setActionBarText() {
        //ActionBar actionBar = this.getActionBar();
        TextView actionBar = (TextView) findViewById(R.id.titlezz);
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            actionBar.setText(getString(R.string.online_init));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP) {
            actionBar.setText(getString(R.string.text_sign_up));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS) {
            actionBar.setText(getString(R.string.text_param_trans));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD) {
            actionBar.setText(getString(R.string.text_status_upload));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_ECHO_TEST) {
            actionBar.setText(getString(R.string.text_echo_test));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
            actionBar.setText(getString(R.string.text_ic_capk_download));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            actionBar.setText(getString(R.string.text_ic_para_download));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
            actionBar.setText(getString(R.string.text_consume));
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME_REVOKE_POSITIVE) {
            actionBar.setText(getString(R.string.text_consume_positive));
        }
    }

    private void showResult() {
        if (mErrorReson > 0) {
            if (mErrorReson == PacketProcessUtils.SOCKET_PROC_ERROR_REASON_IP_PORT) {
                mResponseDetail = getString(R.string.result_error_ip_or_port);
            } else if (mErrorReson == PacketProcessUtils.SOCKET_PROC_ERROR_REASON_CONNE) {
                mResponseDetail = getString(R.string.result_error_conn);
            } else if (mErrorReson == PacketProcessUtils.SOCKET_PROC_ERROR_REASON_SEND) {
                mResponseDetail = getString(R.string.result_error_send);
            } else if (mErrorReson == PacketProcessUtils.SOCKET_PROC_ERROR_REASON_RECE) {
                mResponseDetail = getString(R.string.result_error_rece);
            } else if (mErrorReson == PacketProcessUtils.SOCKET_PROC_ERROR_REASON_RECE_TIME_OUT) {
                mResponseDetail = getString(R.string.result_error_rece_time_out);
            }
            mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
        } else {
            if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
                if (mResponse != null && mResponse.equals("00")) {
                    mResponse = null;
                    String temp = null;
                    if (mResponseDetail != null && mResponseDetail.length() > 2) {
                        temp = mResponseDetail.substring(0, 2);
                        Log.i(TAG, "temp = " + temp);
                    }
                    if (temp != null && temp.equals("01")) {
                        mResponseDetail = mResponseDetail.substring(2);
                        mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
                    } else {
                        mResponseDetail = getString(R.string.result_sucess_online_init);
                        mImageResult.setImageDrawable(getDrawable(R.drawable.trans_success));
                    }
                } else {
                    mResponseDetail = getString(R.string.result_failed_online_init);
                    mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
                }
            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP) {
                if (mResponse != null && mResponse.equals("00")) {
                    mResponse = null;
                    String temp = null;
                    if (mResponseDetail != null && mResponseDetail.length() > 2) {
                        temp = mResponseDetail.substring(0, 2);
                        Log.i(TAG, "temp = " + temp);
                    }
                    if (temp != null && temp.equals("01")) {
                        mResponseDetail = mResponseDetail.substring(2);
                        mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
                    } else {
                        mResponseDetail = getString(R.string.result_sucess_sign_up);
                        mImageResult.setImageDrawable(getDrawable(R.drawable.trans_success));
                    }
                } else {
                    mResponseDetail = getString(R.string.result_failed_sign_up);
                    mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
                }
            } else {
                if (mResponse == null || !mResponse.equals("00")) {
                    if (mResponseDetail == null) {
                        mResponseDetail = getString(R.string.result_error_unkown);
                    }
                    mImageResult.setImageDrawable(getDrawable(R.drawable.trans_faild));
                } else {
                    mResponse = null;

                    if (mProcType == PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS) {
                        mResponseDetail = getString(R.string.result_sucess_para_trans);
                    } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD) {
                        mResponseDetail = getString(R.string.result_sucess_status_upload);
                    } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME_POSITIVE) {
                        mResponseDetail = getString(R.string.result_sucess_consume_positive);
                        ConsumeFieldInfo consumeFieldInfo = new ConsumeFieldInfo(this);
                        consumeFieldInfo.clearField();
                    } else {
                        if (mResponseDetail == null) {
                            mResponseDetail = getString(R.string.result_sucess);
                        }
                    }
                    mImageResult.setImageDrawable(getDrawable(R.drawable.trans_success));
                }
            }
        }

        mTextResponse.setText(mResponse);
        mTextResDetail.setText(mResponseDetail);
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick()");
        switch (v.getId()) {
            case R.id.btn_exit:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAidlLed = DeviceTopUsdkServiceManager.getInstance().getLedManager();
        try {
            if(mAidlLed != null){
                mAidlLed.setLed(0 , false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
