package com.standard.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.cache.ConsumeData;
import com.standard.app.cache.Static;
import com.standard.app.card.CardManager;
import com.standard.app.connect.SocketProcessTask;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.packet.PackEmv;
import com.standard.app.packet.PackOnlineInit;
import com.standard.app.packet.PackPacket;
import com.standard.app.packet.UnpackPacket;
import com.standard.app.packet.UnpackUtils;
import com.standard.app.storage.CommunicationInfo;
import com.standard.app.util.PacketProcessUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicLong;

import static com.standard.app.cache.Static.FINALICCDATA;
import static com.standard.app.cache.Static.cardSequenceNumber_DE23;
import static com.standard.app.cache.Static.dateExpiration_DE14;
import static com.standard.app.cache.Static.iccDATA_DE55;
import static com.standard.app.cache.Static.iccResponceData_DE55;
import static com.standard.app.cache.Static.pinData_DE52;
import static com.standard.app.cache.Static.posConditionCode_DE25;
import static com.standard.app.cache.Static.posDataCode_DE123;
import static com.standard.app.cache.Static.posEntryMode_DE22;
import static com.standard.app.cache.Static.posPINCaptureCode_DE26;
import static com.standard.app.cache.Static.responseCode_DE39;
import static com.standard.app.cache.Static.retrievalReferenceNumber_DE37;
import static com.standard.app.cache.Static.systemsTraceAuditNumber_DE11;
import static com.standard.app.cache.Static.track2Data_DE35;

public class PacketProcessActivity extends Activity {
    private static final String TAG = Utils.TAGPUBLIC + PacketProcessActivity.class.getSimpleName();

    private static final int MSG_TIME_UPDATE = 100;
    private static final long MSG_TIME_UPDATE_DALAY = 1000;

    public TextView mTextProcDetail;
    public TextView mTextProcStatus;
    private TextView mTextTime;

    private SocketProcessTask mSocketProcessTask;
    private CommunicationInfo mCommunicationInfo;
    private PackPacket mPackPacket;
    private UnpackPacket mUnpackPacket;
    private byte[] mSendPacket = null;
    private byte[] mRecePacket = null;
    private String mResponse;
    private String mResponseDetail;

    private Bundle mBundle;
    private int mProcType;
    private int mProcTime = 60;
    private int mProcNum = 1;

    private int mEmvStatus = PackEmv.EMV_DOWNLOAD_STATUS_QUERY;
    private static AtomicLong idCounter = new AtomicLong();

    byte[] PIN = null;
    byte[] KSNVALUE = null;
    byte[] ICCDATA = null;
    byte[] ICCPOSITIVEDATA = null;
    String EXPIREDATA = "";
    String SECONDTRACKDATA = "";
    String SERIALNUMBER = "";
    String THIRDTRACKDATA = "";
    String SCANRESULT = "";
    String AMOUNT = "";
    String CARDNUMBER = "";
    int CARDTYPE = 0;
    int TRANSACTIONTYPE = 0;
    String BUSINESSNAME = "";
    String BUSINESSNUMBER = "";
    String TERMINALNUMBER = "";
    String ACQUIRER = "";
    String REFERENCENUMBER = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "PACKET PROCESS HAS BEEN CALLED = ");
        setContentView(R.layout.activity_packet_process);

        mTextProcDetail = (TextView) findViewById(R.id.proc_detail);
        mTextProcStatus = (TextView) findViewById(R.id.proc_status);
        mTextTime = (TextView) findViewById(R.id.proc_time);
        mHandle.sendEmptyMessageDelayed(MSG_TIME_UPDATE, MSG_TIME_UPDATE_DALAY);

        mBundle = getIntent().getExtras();
        mProcType = mBundle.getInt(PacketProcessUtils.PACKET_PROCESS_TYPE);
        Log.i(TAG, "mProcType = "+mProcType);
        //setActionBarText();

        mCommunicationInfo = new CommunicationInfo(this);

        mPackPacket = new PackPacket(this, mCommunicationInfo.getTPDU());
        mUnpackPacket = new UnpackPacket(this, mProcType);

        if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
            if (!mUnpackPacket.clearEmvCapk()) {
                showResult(mResponse, mResponseDetail, 0);
                return;
            }
            mTextProcDetail.setText(R.string.socket_proc_detail_emv_capk);
            mBundle.putInt("emv_download_status", mEmvStatus);
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            if (!mUnpackPacket.clearEmvPara()) {
                showResult(mResponse, mResponseDetail, 0);
                return;
            }
            mTextProcDetail.setText(R.string.socket_proc_detail_emv_para);
            mBundle.putInt("emv_download_status", mEmvStatus);
        }

        getPacketAndSend(mBundle);
        CardManager.getInstance().finishPreActivity();
    }

    private void setActionBarText() {
        ActionBar actionBar = this.getActionBar();
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.online_init) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_sign_up) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_param_trans) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_status_upload) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_ECHO_TEST) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_echo_test) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_ic_capk_download) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_ic_para_download) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_consume) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME_POSITIVE) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_consume_positive) + ") ");
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SCAN) {
            actionBar.setTitle(getString(R.string.socket_proc_commu) + "(" + getString(R.string.text_scan) + ") ");
        }
    }



    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }

    private void getPacketAndSend(Bundle data) {
        data.toString();
        Log.i(TAG, "NAZUIA HAPAAAAA INTERNET");
        Log.i(TAG, "DATA NI "+data.toString());
        Log.i(TAG, "SEE OTHER DATA");



        PIN = PosApplication.getApp().mConsumeData.getPin();
        KSNVALUE = PosApplication.getApp().mConsumeData.getKsnValue();
        ICCDATA = PosApplication.getApp().mConsumeData.getICData();
        ICCPOSITIVEDATA = PosApplication.getApp().mConsumeData.getICPositiveData();
        EXPIREDATA = PosApplication.getApp().mConsumeData.getExpiryData();
        SECONDTRACKDATA = PosApplication.getApp().mConsumeData.getSecondTrackData();
        SERIALNUMBER = PosApplication.getApp().mConsumeData.getSerialNum();
        THIRDTRACKDATA = PosApplication.getApp().mConsumeData.getThirdTrackData();
        SCANRESULT = PosApplication.getApp().mConsumeData.getScanResult();
        AMOUNT = PosApplication.getApp().mConsumeData.getAmount();
        CARDNUMBER = PosApplication.getApp().mConsumeData.getCardno();
        CARDTYPE = PosApplication.getApp().mConsumeData.getCardType();
        TRANSACTIONTYPE = PosApplication.getApp().mConsumeData.getConsumeType();
        BUSINESSNAME = "Example Business";
        BUSINESSNUMBER = "Example Business Number";
        TERMINALNUMBER = "Example Terminal Number";
        ACQUIRER = "Example Bank";
        REFERENCENUMBER = createID();


        Log.i(TAG, "pin: " + BCDASCII.bytesToHexString(PIN));
        Log.i(TAG, "ksnValue: " + BCDASCII.bytesToHexString(KSNVALUE));
        Log.i(TAG, "ICCDATA: " + BCDASCII.bytesToHexString(ICCDATA));
        Log.i(TAG, "ICCPOSITIVEDATA: " + BCDASCII.bytesToHexString(ICCPOSITIVEDATA));
        Log.i(TAG, "EXPIREDATA: " + EXPIREDATA);
        Log.i(TAG, "SECONDTRACK: " + SECONDTRACKDATA);
        Log.i(TAG, "THIRDTRACK: " + THIRDTRACKDATA);
        Log.i(TAG, "SERIALNUMBER: " + SERIALNUMBER);
        Log.i(TAG, "SCANRESULT: " + SCANRESULT);

        Log.i(TAG, "AMOUNT: " + AMOUNT);
        Log.i(TAG, "CARDNUMBER: " + CARDNUMBER);
        Log.i(TAG, "CARDTYPE: " + CARDTYPE);
        Log.i(TAG, "TRANSACTIONTYPE: " + TRANSACTIONTYPE);

        Log.i(TAG, "posEntryMode_DE22: " + posEntryMode_DE22);
        Log.i(TAG, "posConditionCode_DE25: " + posConditionCode_DE25);
        Log.i(TAG, "systemsTraceAuditNumber_DE11: " + systemsTraceAuditNumber_DE11);
        Log.i(TAG, "posPINCaptureCode_DE26: " + posPINCaptureCode_DE26);

        new doFileUpload().execute();



        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////HAPAAAAAAAAAAAAAAA MUHIMU SANAAA///////////////////////////////

        //processPacket(0);

        //byte[] field47 = mUnpackPacket.getField47();
        //showConsumeSuccResult(mResponse, mResponseDetail, field47);

        /*
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            data.putInt(PackOnlineInit.PACK_ONLINE_INIT_TIME, mProcNum);
        }

        mSendPacket = mPackPacket.getSendPacket(mProcType, data);
        mSocketProcessTask = new SocketProcessTask(this, mProcType);
        mSocketProcessTask.execute(mSendPacket);
        */
    }

    public void onSocketProcessEnd(byte[] recePacket, int errReason) {
        Log.i(TAG, "onSocketProcessEnd");

        if (mBundle.getBoolean("is_need_proc", false)) {
            mHandle.removeMessages(MSG_TIME_UPDATE);
            setResult(1);
            finish();
        } else {
            if (recePacket != null) {
                mRecePacket = recePacket;

                String isNeedProcString = isNeedProcessReq(mRecePacket);

                if (mProcType != PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT && isNeedProcString != null) {
                    Intent intent = new Intent(this, PacketProcessActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("is_need_proc", true);
                    if (isNeedProcString.equals("1")) { //下发终端参数
                        bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS);
                    } else if (isNeedProcString.equals("2")) { //上传终端状态信息
                        bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD);
                    } else if (isNeedProcString.equals("3")) { //重新签到
                        bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_SIGN_UP);
                    } else if (isNeedProcString.equals("4")) { //公钥下载
                        bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD);
                    } else if (isNeedProcString.equals("5")) { //参数下载
                        bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD);
                    }
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 100);
                } else {
                    ///HAPAAAA GO TO SUCCESS, IMEHAMISHIWA IN UPLOAD CLASS
                    //processPacket(errReason);
                }
            } else {
                ///HAAAAPAAAAAAAAAAAAAAAAAAAAAAAAAAA
                showResult(mResponse, mResponseDetail, errReason);
            }
        }
    }

    private String isNeedProcessReq(byte[] recPacket) {

        String tempStr = null;
        if (recPacket != null) {
            int srcDataLen = recPacket.length;
            UnpackUtils unpack = new UnpackUtils();
            ISO8583 mIso = unpack.UnpackFront(recPacket, srcDataLen);
            if(mIso == null)
                return null;

            tempStr = BCDASCII.fromBCDToASCIIString(recPacket, 2 + 5 + 2, 2, false);
            Log.i(TAG, "tempStr: " + tempStr);
            tempStr = tempStr.substring(1);
            if (tempStr.equals("0")) {
                return null;
            } else if(tempStr.equals("7")) { //处理中心通知终端将管理统计数据项清0
                return null;
            } else if(tempStr.equals("8")) { //处理中心下传的密钥更新要求，让终端发起密钥更新操作
                return null;
            } else if(tempStr.equals("6")) { //下载终端程序
                return null;
            }
        }
        return tempStr;
    }

    private void processPacket(int errReason) {
        Bundle data = new Bundle();
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            data.putInt(PackOnlineInit.PACK_ONLINE_INIT_TIME, mProcNum);
        }

        mUnpackPacket.procRecePacket(this, mRecePacket, data);
        mResponse = mUnpackPacket.getResponse();
        mResponseDetail = mUnpackPacket.getResponseDetail();

        if (mResponse != null && mResponse.equals("00")) {
            mProcNum++;
            if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT && mProcNum < 4) {
                String temp = null;
                if (mResponseDetail != null && mResponseDetail.length() > 2) {
                    temp = mResponseDetail.substring(0, 2);
                    Log.i(TAG, "temp = " + temp);
                }
                if (temp != null && temp.equals("01")) {
                    showResult(mResponse, mResponseDetail, errReason);
                } else {
                    getPacketAndSend(data);
                }
            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP && mBundle.getBoolean("is_to_home_page", false)) {
                String temp = null;
                if (mResponseDetail != null && mResponseDetail.length() > 2) {
                    temp = mResponseDetail.substring(0, 2);
                    Log.i(TAG, "temp = " + temp);
                }
                if (temp == null || !temp.equals("01")) {
                    mHandle.removeMessages(MSG_TIME_UPDATE);
                    Intent intent = new Intent(this, CaiMiActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showResult(mResponse, mResponseDetail, errReason);
                }
            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD ||
                    mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
                mEmvStatus = mUnpackPacket.procDownloadEmv(mEmvStatus);
                if (mEmvStatus > 0) {
                    data.putInt("emv_download_status", mEmvStatus);
                    if (mEmvStatus == PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD) {
                        data.putByteArray("emv_field_62", mUnpackPacket.getField62(this));
                    }
                    getPacketAndSend(data);
                } else {
                    if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
                        mResponseDetail = getString(R.string.socket_proc_detail_emv_capk_download_success);
                    } else {
                        mResponseDetail = getString(R.string.socket_proc_detail_emv_para_download_success);
                    }
                    showResult(mResponse, mResponseDetail, errReason);
                }
            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME && (mResponse != null) && (mResponse.equals("00"))) {
                byte[] field47 = mUnpackPacket.getField47();
                showConsumeSuccResult(mResponse, mResponseDetail, field47);

            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SCAN && (mResponse != null) && (mResponse.equals("00"))) {
                byte[] field47 = mUnpackPacket.getField47();
                showScanSuccResult(mResponse, mResponseDetail, field47);

            } else {
                showResult(mResponse, mResponseDetail, errReason);
            }
        } else {
            showResult(mResponse, mResponseDetail, errReason);
        }
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_UPDATE:
                    mProcTime--;
                    mTextTime.setText(String.valueOf(mProcTime));
                    if (mProcTime != 0) {
                        mHandle.sendEmptyMessageDelayed(MSG_TIME_UPDATE, MSG_TIME_UPDATE_DALAY);
                    } else {
                        showResult(mResponse, mResponseDetail, PacketProcessUtils.SOCKET_PROC_ERROR_REASON_RECE_TIME_OUT);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void showConsumeSuccResult(String response, String resDetail, byte[] printDetail) {
        Log.i(TAG, "showSuccessResult(), response = "+response+", resDetail = "+resDetail+", printDetail = "+printDetail);
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME &&
                PosApplication.getApp().mConsumeData.getCardType() != ConsumeData.CARD_TYPE_MAG) {
            CardManager.getInstance().setRequestOnline(true, mResponse, BCDASCII.bytesToHexString(PosApplication.getApp().mConsumeData.getICData()));
        }
        mHandle.removeMessages(MSG_TIME_UPDATE);
        Intent intent = new Intent(this, ConsumeSuccessActivity.class);
        intent.putExtra("result_response", response);
        intent.putExtra("result_resDetail", resDetail);
        intent.putExtra("result_field47", printDetail);
        startActivity(intent);
        this.finish();
    }

    private void showScanSuccResult(String response, String resDetail, byte[] printDetail) {
        Log.i(TAG, "showSuccessResult(), response = "+response+", resDetail = "+resDetail+", printDetail = "+printDetail);
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME &&
                PosApplication.getApp().mConsumeData.getCardType() != ConsumeData.CARD_TYPE_MAG) {
            CardManager.getInstance().setRequestOnline(true, mResponse, BCDASCII.bytesToHexString(PosApplication.getApp().mConsumeData.getICData()));
        }
        mHandle.removeMessages(MSG_TIME_UPDATE);
        Intent intent = new Intent(this, ScanSuccessActivity.class);
        intent.putExtra("result_response", response);
        intent.putExtra("result_resDetail", resDetail);
        intent.putExtra("result_field47", printDetail);
        startActivity(intent);
        this.finish();
    }

    private void showResult(String response, String resDetail, int errReason) {
        Log.i(TAG, "showResult(), response = "+response+", resDetail = "+resDetail+", errReason = "+errReason);
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME &&
                PosApplication.getApp().mConsumeData.getCardType() != ConsumeData.CARD_TYPE_MAG) {
            CardManager.getInstance().setRequestOnline(true, mResponse, BCDASCII.bytesToHexString(PosApplication.getApp().mConsumeData.getICData()));
        }
        mHandle.removeMessages(MSG_TIME_UPDATE);
        Intent intent = new Intent(this, ShowResultActivity.class);
        intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, mProcType);
        intent.putExtra("result_response", mResponse);
        intent.putExtra("result_resDetail", mResponseDetail);
        intent.putExtra("result_errReason", errReason);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            processPacket(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSocketProcessTask != null) {
            mSocketProcessTask.setStop();
            mSocketProcessTask.cancel(true);
        }
    }










    public class doFileUpload extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.e(TAG, "STARTING PRE UPLOAD");

            int mErrorReson = 0;
            if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
                mRecePacket = BCDASCII.hexStringToBytes("038460000006038020000000003939303630353035353333363030303030303030303038333030303030303030303030303033303030303030303039393036303530353533333630303030303030303030300210303C04C10EC68813009000000000000008000031153530091949120210000608999900013135333533303230353030323230343030313030303030303030303839393036303530353533333630303000885F573131313137303333373030303030303938363202BDADCEF7C5A9D0C5C1AABACFC9E7202002343832313030303102490230025F5109BDBBD2D7B3C9B9A6025F551530303030303102303030303331023039313902300206118F0382025E10B0E6B1BE3A3134303432390AC9CCBBA7C3FB3ABDF8CFCDC3C5B5EA5FB2E2CAD4330AC9CCBBA7BAC53A3939303630353035353333363030300AD6D5B6CBBAC53A30303030303030382020B2D9D7F7D4B13A30310AB7A2BFA8BBFAB9B93ABDADCEF7C5A9D0C5C1AABACFC9E720200ACAD5B5A5BBFAB9B93A34383231303030310A7EBFA8BAC53A3632323638322A2A2A2A2A2A2A2A2A3732343020537E0A7EBDBBD2D7C0E0D0CD3ACFFBB7D17E0AD3D0D0A7C6DA3A34393132202020C5FAB4CEBAC53A3030303030310AC6BED6A4BAC53A30303030333120CADAC8A8C2EB3A3230343030310ABDBBD2D7B2CEBFBCBAC53A3135333533303230353030320ACAB1BCE43A323031372D30392D31392031353A33353A33300A7EBDF0B6EE3A524D4220302E30387E0A2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ABDBBD2D7B5A5BAC53A3131313730333337303030303030393836322020420A2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0A02117EB3D6BFA8C8CBC7A9C3FB313A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A0212CDA8C1AABFCDBBA7BAC53A3939303630353035353333363030300A7EB3D6BFA8C8CBC7A9C3FB3A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A0213CDA8C1AABFCDBBA7BAC53A3939303630353035353333363030300A7EB3D6BFA8C8CBC7A9C3FB3A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A02313439260000000000000000192200000100050000000000034355504243343337323036");
                mErrorReson = 0;
            } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SCAN) {
                mRecePacket = BCDASCII.hexStringToBytes("02DA600000060380200000000039393036303530353533333630303030303030303030383330303030303030303030303030333030303030303030393930363035303535333336303030303030303030303002103038048102C60013400100000000000001000020180649111803000008999900013030303030303030303839393036303530353533333630303000895F5109BDBBD2D7B3C9B9A6025F554A313131380230303030303102313131373033333730303030303133393639023131313730333337303030303031333936390256535035313102323038383930323432373931353235320204758F038201D610B3CCD0F2B0E6B1BEBAC53A3134303432390A7EC9CCBBA7C3FBB3C63ABDF8CFCDC3C5B5EA5FB2E2CAD4337E0A7EC9CCBBA7BAC53A3939303630353035353333363030307E0A7ED6D5B6CBBAC53A303030303030303820207E0AB2D9D7F7D4B1BAC53A30310AD6A7B8B6D5CBBAC53A0ABDBBD2D7B5A5BAC53A3131313730333337303030303031333936390ABDBBD2D7B2CEBFBCBAC53A303531363433323539380A7EBDBBD2D7C0E0D0CD3AD6A7B8B6B1A6D6A7B8B67E0A7EBDF0B6EE3A524D423A302E30317E0ABDBBD2D7CAB1BCE43A323031372D31312D31382031383A30363A35332002112D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A02122D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A02132D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A0200145200000100000000034149503542394539413136");
                mErrorReson = 0;
            }

            // test end
            onSocketProcessEnd(mRecePacket, mErrorReson);





        }

        @Override
        protected String doInBackground(String... String) {
            String theresult = "";
            String URLz = "http://greenhut.jagro.co.tz/pos/saveTransactions.php";

            Log.e("RESULT FROM SERVER", "TUPO BACKGROUND");


            try{



                String link=URLz;
                String data  = URLEncoder.encode("REFERENCENUMBER", "UTF-8") + "=" + URLEncoder.encode(REFERENCENUMBER, "UTF-8");
                data += "&" + URLEncoder.encode("ACQUIRER", "UTF-8") + "=" + URLEncoder.encode(ACQUIRER, "UTF-8");
                data += "&" + URLEncoder.encode("TERMINALNUMBER", "UTF-8") + "=" + URLEncoder.encode(TERMINALNUMBER, "UTF-8");
                data += "&" + URLEncoder.encode("BUSINESSNUMBER", "UTF-8") + "=" + URLEncoder.encode(BUSINESSNUMBER, "UTF-8");
                data += "&" + URLEncoder.encode("BUSINESSNAME", "UTF-8") + "=" + URLEncoder.encode(BUSINESSNAME, "UTF-8");
                data += "&" + URLEncoder.encode("TRANSACTIONTYPE", "UTF-8") + "=" + URLEncoder.encode(java.lang.String.valueOf(TRANSACTIONTYPE), "UTF-8");
                data += "&" + URLEncoder.encode("CARDTYPE", "UTF-8") + "=" + URLEncoder.encode(java.lang.String.valueOf(CARDTYPE), "UTF-8");
                data += "&" + URLEncoder.encode("CARDNUMBER", "UTF-8") + "=" + URLEncoder.encode(CARDNUMBER, "UTF-8");
                data += "&" + URLEncoder.encode("AMOUNT", "UTF-8") + "=" + URLEncoder.encode(AMOUNT, "UTF-8");
                data += "&" + URLEncoder.encode("SCANRESULT", "UTF-8") + "=" + URLEncoder.encode("SCANRESULT", "UTF-8");
                data += "&" + URLEncoder.encode("THIRDTRACKDATA", "UTF-8") + "=" + URLEncoder.encode("THIRDTRACKDATA", "UTF-8");
                data += "&" + URLEncoder.encode("SERIALNUMBER", "UTF-8") + "=" + URLEncoder.encode(SERIALNUMBER, "UTF-8");
                data += "&" + URLEncoder.encode("SECONDTRACKDATA", "UTF-8") + "=" + URLEncoder.encode(SECONDTRACKDATA, "UTF-8");
                data += "&" + URLEncoder.encode("EXPIREDATA", "UTF-8") + "=" + URLEncoder.encode(EXPIREDATA, "UTF-8");
                data += "&" + URLEncoder.encode("ICCPOSITIVEDATA", "UTF-8") + "=" + URLEncoder.encode(BCDASCII.bytesToHexString(ICCPOSITIVEDATA), "UTF-8");
                data += "&" + URLEncoder.encode("ICCDATA", "UTF-8") + "=" + FINALICCDATA;
                data += "&" + URLEncoder.encode("KSNVALUE", "UTF-8") + "=" + URLEncoder.encode("KSNVALUE", "UTF-8");
                data += "&" + URLEncoder.encode("PIN", "UTF-8") + "=" + URLEncoder.encode(BCDASCII.bytesToHexString(PIN), "UTF-8");
                data += "&" + URLEncoder.encode("posDataCode_DE123", "UTF-8") + "=" + URLEncoder.encode(posDataCode_DE123, "UTF-8");
                data += "&" + URLEncoder.encode("posEntryMode_DE22", "UTF-8") + "=" + URLEncoder.encode(posEntryMode_DE22, "UTF-8");
                data += "&" + URLEncoder.encode("posConditionCode_DE25", "UTF-8") + "=" + URLEncoder.encode(posConditionCode_DE25, "UTF-8");
                data += "&" + URLEncoder.encode("systemsTraceAuditNumber_DE11", "UTF-8") + "=" + URLEncoder.encode(systemsTraceAuditNumber_DE11, "UTF-8");
                data += "&" + URLEncoder.encode("posPINCaptureCode_DE26", "UTF-8") + "=" + URLEncoder.encode(posPINCaptureCode_DE26, "UTF-8");
                data += "&" + URLEncoder.encode("retrievalReferenceNumber_DE37", "UTF-8") + "=" + URLEncoder.encode(retrievalReferenceNumber_DE37, "UTF-8");
                data += "&" + URLEncoder.encode("dateExpiration_DE14", "UTF-8") + "=" + URLEncoder.encode(dateExpiration_DE14, "UTF-8");
                data += "&" + URLEncoder.encode("responseCode_DE39", "UTF-8") + "=" + URLEncoder.encode(responseCode_DE39, "UTF-8");





                Log.e("THE DATA", "DATA "+data);

                java.net.URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write( data );
                wr.flush();

                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                theresult = sb.toString();
                //return sb.toString();
                Log.e("RESULT FROM SERVER", "Response from url: " + sb.toString());
            } catch(Exception e){
                //return new String("Exception: " + e.getMessage());
                Log.e("RESULT FROM SERVER", "Exception: " + e.getMessage());
            }



            return theresult;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        if(result !=null){
            if(result.equalsIgnoreCase("100X")){
                /*
                int mErrorReson = 0;
                if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
                    mRecePacket = BCDASCII.hexStringToBytes("038460000006038020000000003939303630353035353333363030303030303030303038333030303030303030303030303033303030303030303039393036303530353533333630303030303030303030300210303C04C10EC68813009000000000000008000031153530091949120210000608999900013135333533303230353030323230343030313030303030303030303839393036303530353533333630303000885F573131313137303333373030303030303938363202BDADCEF7C5A9D0C5C1AABACFC9E7202002343832313030303102490230025F5109BDBBD2D7B3C9B9A6025F551530303030303102303030303331023039313902300206118F0382025E10B0E6B1BE3A3134303432390AC9CCBBA7C3FB3ABDF8CFCDC3C5B5EA5FB2E2CAD4330AC9CCBBA7BAC53A3939303630353035353333363030300AD6D5B6CBBAC53A30303030303030382020B2D9D7F7D4B13A30310AB7A2BFA8BBFAB9B93ABDADCEF7C5A9D0C5C1AABACFC9E720200ACAD5B5A5BBFAB9B93A34383231303030310A7EBFA8BAC53A3632323638322A2A2A2A2A2A2A2A2A3732343020537E0A7EBDBBD2D7C0E0D0CD3ACFFBB7D17E0AD3D0D0A7C6DA3A34393132202020C5FAB4CEBAC53A3030303030310AC6BED6A4BAC53A30303030333120CADAC8A8C2EB3A3230343030310ABDBBD2D7B2CEBFBCBAC53A3135333533303230353030320ACAB1BCE43A323031372D30392D31392031353A33353A33300A7EBDF0B6EE3A524D4220302E30387E0A2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ABDBBD2D7B5A5BAC53A3131313730333337303030303030393836322020420A2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0A02117EB3D6BFA8C8CBC7A9C3FB313A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A0212CDA8C1AABFCDBBA7BAC53A3939303630353035353333363030300A7EB3D6BFA8C8CBC7A9C3FB3A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A0213CDA8C1AABFCDBBA7BAC53A3939303630353035353333363030300A7EB3D6BFA8C8CBC7A9C3FB3A7E0A0A0AB1BEC8CBC8B7C8CFD2D4C9CFBDBBD2D72CCDACD2E2BDABC6E4BCC6C8EBB1BED5CBBBA70A02313439260000000000000000192200000100050000000000034355504243343337323036");
                    mErrorReson = 0;
                } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SCAN) {
                    mRecePacket = BCDASCII.hexStringToBytes("02DA600000060380200000000039393036303530353533333630303030303030303030383330303030303030303030303030333030303030303030393930363035303535333336303030303030303030303002103038048102C60013400100000000000001000020180649111803000008999900013030303030303030303839393036303530353533333630303000895F5109BDBBD2D7B3C9B9A6025F554A313131380230303030303102313131373033333730303030303133393639023131313730333337303030303031333936390256535035313102323038383930323432373931353235320204758F038201D610B3CCD0F2B0E6B1BEBAC53A3134303432390A7EC9CCBBA7C3FBB3C63ABDF8CFCDC3C5B5EA5FB2E2CAD4337E0A7EC9CCBBA7BAC53A3939303630353035353333363030307E0A7ED6D5B6CBBAC53A303030303030303820207E0AB2D9D7F7D4B1BAC53A30310AD6A7B8B6D5CBBAC53A0ABDBBD2D7B5A5BAC53A3131313730333337303030303031333936390ABDBBD2D7B2CEBFBCBAC53A303531363433323539380A7EBDBBD2D7C0E0D0CD3AD6A7B8B6B1A6D6A7B8B67E0A7EBDF0B6EE3A524D423A302E30317E0ABDBBD2D7CAB1BCE43A323031372D31312D31382031383A30363A35332002112D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A02122D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A02132D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0ACEDED0E8C3DCC2EB2FCEDED0E8C7A9C3FB0AD0EBD6AA3AD6A7B8B6B3C9B9A62CC7EBC1F4D2E2D7CABDF0B1E4BBAFA1A30A0200145200000100000000034149503542394539413136");
                    mErrorReson = 0;
                }

                // test end
                onSocketProcessEnd(mRecePacket, mErrorReson);

                 */
                int errReason = 0;
                processPacket(errReason);
            }else{

            }
        }else{

        }




        }
    }











}
