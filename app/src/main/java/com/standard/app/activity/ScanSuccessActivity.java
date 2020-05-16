package com.standard.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.print.ConsumePrint;
import com.topwise.cloudpos.data.PrinterConstant;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScanSuccessActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + ScanSuccessActivity.class.getSimpleName();

    private static final int MSG_TIME_UPDATE = 100;
    public static final int MSG_TASK_SHOW_RESULT = 101;
    public static final int MSG_TASK_PRINT = 103;

    private TextView mScanAmt;
    private TextView mScanType;
    private TextView mScanPayNum;
    private TextView mScanTradNum;
    private TextView mScanReference;
    private TextView mOperatorNum;
    private TextView mScanTime;

//    private TransactionSub mTransaction;

    private Bundle mBundle;
    private byte[] mField47;

    private String mShowMsg;
    private String mPrintHolder;
    private String mPrintMerchant;
    private String mPrintBank;

    private ConsumePrint mConsumePrint;
    private AlertDialog.Builder mAlertDialog;

    private int mTime = 31;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_scan_result);

        mScanAmt = (TextView) findViewById(R.id.scan_amount);
        mScanType = (TextView) findViewById(R.id.scan_type);
        mScanPayNum = (TextView) findViewById(R.id.scan_pay_dnum);
        mScanTradNum = (TextView) findViewById(R.id.scan_trad_num);
        mScanReference = (TextView) findViewById(R.id.scan_reference_num);
        mOperatorNum = (TextView) findViewById(R.id.operator_num);
        mScanTime = (TextView) findViewById(R.id.scan_time);

        mConsumePrint = new ConsumePrint(null, this);
        mConsumePrint.setCurTime(getCurTime());
        mBundle = this.getIntent().getExtras();
        mField47 = mBundle.getByteArray("result_field47");

        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.title_consume);

//        mTransaction = TransactionSub.getInstance();

        mAlertDialog = new AlertDialog.Builder(this);
        getPrintMessage(mField47);
        //add by zongli for fake data
        //showScanData(mShowMsg);
          showScanFakeData(mShowMsg);
        //add end

        mHandle.sendEmptyMessage(MSG_TIME_UPDATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_print:
                try {
                    int printState = mConsumePrint.mPrinterManager.getPrinterState();
                    Log.i(TAG, "printState = "+printState);

                    if (printState == PrinterConstant.PrinterState.PRINTER_STATE_NOPAPER) {
                        showDialog(null, getString(R.string.result_need_paper));
                    } else {
                        mConsumePrint.printDetail(mPrintHolder);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }

    public Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_UPDATE:
                    mTime--;
                    if (mTime == 0) {
                        finish();
                    } else {
                        mHandle.sendEmptyMessageDelayed(MSG_TIME_UPDATE, 1000);
                    }
                    break;
                case MSG_TASK_SHOW_RESULT:
                    showDialog(null, msg.getData().getString("message"));
                    break;
                case MSG_TASK_PRINT:
                    showPrintDialog();
                    break;
                default:
                    break;
            }
        }
    };

    private void getPrintMessage(byte[] field47) {
        Log.i(TAG, "getPrintMessage, field47 " + BCDASCII.bytesToHexString(field47));

        byte[] printData = null;
        byte[] printConfirmHolder = null;
        byte[] printConfirmMerchant = null;
        byte[] printConfirmBank = null;

        int startIndex = 0;
        int endIndex = 0;
        int index = 0;

        int printLen = field47.length - 5;
        byte[] printByte = new byte[printLen];
        System.arraycopy(field47, 5, printByte, 0, printLen);
        Log.i(TAG,"printByte: "+BCDASCII.bytesToHexString(printByte));
        try {
            Log.i(TAG,"printByte: "+(new String(printByte, "GBK")));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < printLen; i++) {
            if (printByte[i] == 0x10 || printByte[i] == 0x11
                    || printByte[i] == 0x12 || printByte[i] == 0x13) {
                Log.i(TAG, "1__i = " + i + ", printByte[i] = " + printByte[i]);
                startIndex = i+1;
            }
            if (printByte[i] == 0x02) {
                Log.i(TAG, "2__i = " + i + ", printByte[i] = " + printByte[i]);
                endIndex = i;
                if (index == 0) {
                    Log.i(TAG, "0 = "+(endIndex-startIndex));
                    printData = new byte[endIndex-startIndex];
                    System.arraycopy(printByte, startIndex, printData, 0, endIndex-startIndex);
                    index++;
                } else if (index == 1) {
                    Log.i(TAG, "1 = "+(endIndex-startIndex));
                    printConfirmHolder = new byte[endIndex-startIndex];
                    System.arraycopy(printByte, startIndex, printConfirmHolder, 0, endIndex-startIndex);
                    index++;
                } else if (index == 2) {
                    Log.i(TAG, "2 = "+(endIndex-startIndex));
                    printConfirmMerchant = new byte[endIndex-startIndex];
                    System.arraycopy(printByte, startIndex, printConfirmMerchant, 0, endIndex-startIndex);
                    index++;
                } else if (index == 3) {
                    Log.i(TAG, "3 = "+(endIndex-startIndex));
                    printConfirmBank = new byte[endIndex-startIndex];
                    System.arraycopy(printByte, startIndex, printConfirmBank, 0, endIndex-startIndex);
                    index++;
                }
            }
        }

        try {
            mShowMsg = new String(printData, "GBK");

            mPrintHolder = "POS签购单\n==交易凭证(持卡人联)==\n" + mShowMsg + new String(printConfirmHolder, "GBK")+"\n\n\n\n\n";
            mPrintMerchant = "POS签购单\n===交易凭证(商户联)===\n" + mShowMsg + new String(printConfirmMerchant, "GBK")+"\n\n\n\n\n";
            mPrintBank = "POS签购单\n===交易凭证(银行联)===\n" + mShowMsg + new String(printConfirmBank, "GBK")+"\n\n\n\n\n";
            Log.d(TAG, "mShowMsg = " + mShowMsg);
            Log.d(TAG, "mPrintHolder = " + mPrintHolder);
            Log.d(TAG, "mPrintMerchant = " + mPrintMerchant);
            Log.d(TAG, "mPrintBank = " + mPrintBank);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void showScanData(String printMsg) {
        String[] print = printMsg.replace("~", "").split("\\n");
        String data;
        String[] datas;
        for (int i = 0; i < print.length; i++) {
            if (print[i].indexOf("金额") != -1) {
                datas = print[i].split(":");
                mScanAmt.setText(datas[2]);
            } else if (print[i].indexOf("交易类型") != -1) {
                data = print[i].replace("交易类型:", "");
                mScanType.setText(data);
            } else if (print[i].indexOf("支付账号") != -1) {
                data = print[i].replace("支付账号:", "");
                mScanPayNum.setText(data);
            } else if (print[i].indexOf("交易单号") != -1) {
                data = print[i].replace("交易单号:", "");
                mScanTradNum.setText(data);
            } else if (print[i].indexOf("操作员") != -1) {
                data = print[i].substring(print[i].length()-2, print[i].length());
                mOperatorNum.setText(data);
            } else if (print[i].indexOf("交易参考号") != -1) {
                data = print[i].replace("交易参考号:", "");
                mScanReference.setText(data);
            } else if (print[i].indexOf("时间") != -1) {
                data = print[i].replace("时间:", "");
                mScanTime.setText(data);
            }
        }
    }

    /**add by zongli for fake show data */
    private void showScanFakeData(String printMsg) {
        String[] print = printMsg.replace("~", "").split("\\n");
        String amount = PosApplication.getApp().mConsumeData.getAmount();
        String data;
        String[] datas;
        for (int i = 0; i < print.length; i++) {
            if (print[i].indexOf("金额") != -1) {
                datas = print[i].split(":");
                if(amount != null){
                    mScanAmt.setText(amount);
                } else {
                    mScanAmt.setText(datas[2]);
                }
            } else if (print[i].indexOf("交易类型") != -1) {
                data = print[i].replace("交易类型:", "");
                mScanType.setText(R.string.text_scan);
            } else if (print[i].indexOf("支付账号") != -1) {
                data = print[i].replace("支付账号:", "");
                mScanPayNum.setText(data);
            } else if (print[i].indexOf("交易单号") != -1) {
                data = print[i].replace("交易单号:", "");
                mScanTradNum.setText(data);
            } else if (print[i].indexOf("操作员") != -1) {
                data = print[i].substring(print[i].length()-2, print[i].length());
                mOperatorNum.setText(data);
            } else if (print[i].indexOf("交易参考号") != -1) {
                data = print[i].replace("交易参考号:", "");
                mScanReference.setText(data);
            } else if (print[i].indexOf("时间") != -1) {
                data = print[i].replace("时间:", "");
                if(mConsumePrint != null && mConsumePrint.getCurTime() != null){
                    mScanTime.setText(mConsumePrint.getCurTime());
                } else {
                    mScanTime.setText(data);
                }
            }
        }
    }

    private String getCurTime(){
        Date date =new Date(System.currentTimeMillis());
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);
        return time;
    }
    /**add end */

    private void showDialog(String title, String message) {
        if (mAlertDialog != null) {
            mAlertDialog = new AlertDialog.Builder(this);
            mAlertDialog.setMessage(message);
            mAlertDialog.setCancelable(false);
            mAlertDialog.setPositiveButton(R.string.result_dialog_ok, null);
            mAlertDialog.show();
        }
    }

    private void showPrintDialog() {
        if (mAlertDialog != null) {
            mAlertDialog = new AlertDialog.Builder(this);
            mAlertDialog.setMessage(getString(R.string.result_print_more));
            mAlertDialog.setCancelable(false);
            mAlertDialog.setNegativeButton(R.string.result_dialog_cancle, null);
            mAlertDialog.setPositiveButton(R.string.result_dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int printState = mConsumePrint.mPrinterManager.getPrinterState();
                        Log.i(TAG, "printState = "+printState);

                        if (printState == PrinterConstant.PrinterState.PRINTER_STATE_NOPAPER) {
                            showDialog(null, getString(R.string.result_need_paper));
                        } else {
                            mConsumePrint.printDetail(mPrintMerchant + mPrintBank);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
            mAlertDialog.show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mTime = 30;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        mHandle.removeMessages(MSG_TIME_UPDATE);
        mHandle.removeMessages(MSG_TASK_SHOW_RESULT);
        mHandle.removeMessages(MSG_TASK_PRINT);

        if (mAlertDialog != null) {
            mAlertDialog = null;
        }
    }
}