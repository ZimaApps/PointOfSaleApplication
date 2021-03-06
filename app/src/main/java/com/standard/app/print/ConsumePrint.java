package com.standard.app.print;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.activity.ConsumeSuccessActivity;
import com.standard.app.activity.ScanSuccessActivity;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener;
import com.topwise.cloudpos.aidl.printer.PrintItemObj;
import com.topwise.cloudpos.data.PrinterConstant;

import java.util.ArrayList;

public class ConsumePrint {
    private static final String TAG = Utils.TAGPUBLIC + ConsumePrint.class.getSimpleName();

    public static final int MSG_TASK_SHOW_RESULT = 101;
    public static final int MSG_TASK_PRINT = 103;

    public AidlPrinter mPrinterManager;
    private ArrayList<PrintItemObj> mPrintObjs;
    private PrintItemObj mPrintItem1;
    private PrintItemObj mPrintItem2;
    private PrintItemObj mPrintItem3;

    private boolean isHolder = false;

    private ConsumeSuccessActivity mConsumeSuccessActivity;
    private ScanSuccessActivity mScanSuccessActivity;
    private Context mContext;

    public String curTime;

    public ConsumePrint(ConsumeSuccessActivity consumeSuccessActivity, ScanSuccessActivity scanSuccessActivity) {
        mPrinterManager = DeviceTopUsdkServiceManager.getInstance().getPrintManager();
        mPrintObjs = new ArrayList<PrintItemObj>();

        mConsumeSuccessActivity = consumeSuccessActivity;
        mScanSuccessActivity = scanSuccessActivity;
    }

    public void printDetail(String printMsg) {
        Log.i(TAG, "printDetail, printMsg = "+printMsg);

        if (mConsumeSuccessActivity != null) {
            mContext = mConsumeSuccessActivity;
            //getConsumePrintString(printMsg);
            getConsumePrintFakeString();
        }

        if (mScanSuccessActivity != null) {
            mContext = mScanSuccessActivity;
            //getScanPrintString(printMsg);
            getScanPrintFakeString();
        }

        Log.i(TAG, "startPrint ");

        try {
            mPrinterManager.printText(mPrintObjs, new AidlPrinterListener.Stub(){
                @Override
                public void onPrintFinish() throws RemoteException {
                    Log.i(TAG,"onPrintFinish");

//                    if (isHolder) {
//                        if (mConsumeSuccessActivity != null) {
//                            mConsumeSuccessActivity.mHandle.sendEmptyMessage(MSG_TASK_PRINT);
//                        }
//                        if (mScanSuccessActivity != null) {
//                            mScanSuccessActivity.mHandle.sendEmptyMessage(MSG_TASK_PRINT);
//                        }
//                    } else {
//                        Message message = new Message();
//                        message.what = MSG_TASK_SHOW_RESULT;
//                        Bundle data = new Bundle();
//                        data.putString("message", mContext.getString(R.string.result_print_success));
//                        message.setData(data);
//                        if (mConsumeSuccessActivity != null) {
//                            mConsumeSuccessActivity.mHandle.sendMessage(message);
//                        }
//                        if (mScanSuccessActivity != null) {
//                            mScanSuccessActivity.mHandle.sendMessage(message);
//                        }
//                    }
                }

                @Override
                public void onError(int errorCode) throws RemoteException {
                    Log.i(TAG,"onError:"+errorCode);

                    if (errorCode == PrinterConstant.PrinterState.PRINTER_STATE_NOPAPER) {
                        Message message = new Message();
                        message.what = MSG_TASK_SHOW_RESULT;
                        Bundle data = new Bundle();
                        data.putString("message", mContext.getString(R.string.result_check_paper));
                        message.setData(data);
                        if (mConsumeSuccessActivity != null) {
                            mConsumeSuccessActivity.mHandle.sendMessage(message);
                        }
                        if (mScanSuccessActivity != null) {
                            mScanSuccessActivity.mHandle.sendMessage(message);
                        }
                    } else {
                        Message message = new Message();
                        message.what = MSG_TASK_SHOW_RESULT;
                        Bundle data = new Bundle();
                        data.putString("message", String.valueOf(errorCode));
                        message.setData(data);
                        if (mConsumeSuccessActivity != null) {
                            mConsumeSuccessActivity.mHandle.sendMessage(message);
                        }
                        if (mScanSuccessActivity != null) {
                            mScanSuccessActivity.mHandle.sendMessage(message);
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "startPrint end");
    }

    private void getConsumePrintString(String printMsg) {
        Log.i(TAG, "getConsumePrintString()");
        /**add by zongli for fake print data */
        String amount = PosApplication.getApp().mConsumeData.getAmount();
        String cardNo = PosApplication.getApp().mConsumeData.getCardno();
        String firCardNo = null;
        String mid = null;
        String lastCardNo = null;
        if(cardNo != null){
            int cardLength = cardNo.length();
            firCardNo = cardNo.substring(0,6);
            lastCardNo = cardNo.substring(cardLength - 4);
            mid = "******";
            cardNo = firCardNo + mid + lastCardNo;
        }
        /**add end */
        String[] print = printMsg.split("\\n");
        for (int i = 0; i < print.length; i++) {
            if (print[i].indexOf("~") != -1) {
                if (print[i].indexOf("持卡人签名") != -1) {
                    mPrintObjs.add(getPrintItemObjs("持卡人签名:", PrinterConstant.FontSize.LARGE, PrintItemObj.ALIGN.LEFT));
                }
                /**add by zongli for fake print data */
                else if(print[i].indexOf("卡号") != -1){
                    if(cardNo != null){
                        mPrintObjs.add(getPrintItemObjs("卡号:"+cardNo, PrinterConstant.FontSize.NORMAL,PrintItemObj.ALIGN.LEFT));
                    }
                } else if(print[i].indexOf("金额") != -1){
                    if(amount != null){
                        mPrintObjs.add(getPrintItemObjs("金额:RMB "+amount, PrinterConstant.FontSize.LARGE,PrintItemObj.ALIGN.LEFT));
                    }
                }
                /**add end */
                else {
                    mPrintObjs.add(getPrintItemObjs(print[i].replace("~", ""), PrinterConstant.FontSize.LARGE, PrintItemObj.ALIGN.LEFT));
                }
            } else if (print[i].indexOf("签购单") != -1 ||
                    print[i].indexOf("交易凭证") != -1) {
                if (print[i].indexOf("交易凭证") != -1 && print[i].indexOf("持卡人联") != -1) {
                    isHolder = true;
                } else {
                    isHolder = false;
                }
                mPrintObjs.add(getPrintItemObjs(print[i], PrinterConstant.FontSize.LARGE, PrintItemObj.ALIGN.CENTER));
            }
            /**add by zongli for fake print data */
            else if(print[i].indexOf("时间") != -1){
                if(curTime != null){
                    mPrintObjs.add(getPrintItemObjs("时间:"+curTime, PrinterConstant.FontSize.NORMAL,PrintItemObj.ALIGN.LEFT));
                }
            }
            /**add end */
            else {
                mPrintObjs.add(getPrintItemObjs(print[i], PrinterConstant.FontSize.NORMAL, PrintItemObj.ALIGN.LEFT));
            }
        }
        mPrintObjs.add(getPrintItemObjs("\n\n\n\n", PrinterConstant.FontSize.NORMAL, PrintItemObj.ALIGN.LEFT));
    }

    private void getScanPrintString(String printMsg) {

        /**add by zongli for fake print data */
        String amount = PosApplication.getApp().mConsumeData.getAmount();
        /**add end */

        String[] print = printMsg.split("\\n");
        for (int i = 0; i < print.length; i++) {
            if (print[i].indexOf("~") != -1) {
                if (print[i].indexOf("交易类型") != -1) {
                    mPrintObjs.add(getPrintItemObjs(print[i].replace("~", ""), PrinterConstant.FontSize.LARGE, PrintItemObj.ALIGN.LEFT));
                }
                /**add by zongli for fake print data */
                else if(print[i].indexOf("金额") != -1){
                    if(amount != null){
                        mPrintObjs.add(getPrintItemObjs("金额:RMB "+amount, PrinterConstant.FontSize.LARGE,PrintItemObj.ALIGN.LEFT));
                    }
                }
                /**add end */
                else {
                    mPrintObjs.add(getPrintItemObjs(print[i].replace("~", ""), PrinterConstant.FontSize.NORMAL, PrintItemObj.ALIGN.LEFT));
                }
            } else if (print[i].indexOf("签购单") != -1 ||
                    print[i].indexOf("交易凭证") != -1) {
                if (print[i].indexOf("交易凭证") != -1 && print[i].indexOf("持卡人联") != -1) {
                    isHolder = true;
                } else {
                    isHolder = false;
                }
                mPrintObjs.add(getPrintItemObjs(print[i], PrinterConstant.FontSize.LARGE, PrintItemObj.ALIGN.CENTER));
            } /**add by zongli for fake print data */
            else if(print[i].indexOf("时间") != -1){
                if(curTime != null){
                    mPrintObjs.add(getPrintItemObjs("交易时间:"+curTime, PrinterConstant.FontSize.NORMAL,PrintItemObj.ALIGN.LEFT));
                }
            }
            /**add end */
            else {
                mPrintObjs.add(getPrintItemObjs(print[i], PrinterConstant.FontSize.NORMAL, PrintItemObj.ALIGN.LEFT));
            }
        }
        mPrintObjs.add(getPrintItemObjs("\n\n\n\n", PrinterConstant.FontSize.NORMAL, PrintItemObj.ALIGN.LEFT));
    }

    private void getConsumePrintFakeString() {
        Log.i(TAG, "getConsumePrintString()");
        /**add by zongli for fake print data */
        final String amount = PosApplication.getApp().mConsumeData.getAmount();
        String cardNo = PosApplication.getApp().mConsumeData.getCardno();
        String firCardNo = null;
        String mid = null;
        String lastCardNo = null;
        if(cardNo != null){
            int cardLength = cardNo.length();
            firCardNo = cardNo.substring(0,6);
            lastCardNo = cardNo.substring(cardLength - 4);
            mid = "******";
            cardNo = firCardNo + mid + lastCardNo;
        }
        /**add end */
        final String finalCardNo = cardNo;
        mPrintObjs = new ArrayList<PrintItemObj>() {
            {
                add(new PrintItemObj(mContext.getString(R.string.print_purchase_order), 16, true, PrintItemObj.ALIGN.CENTER));
                add(new PrintItemObj(mContext.getString(R.string.print_transaction_documents), 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_version), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_business_name), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_business_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_terminal_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_acquirer), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_card_number)+ finalCardNo, 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_transaction_type), 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_validity), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_document_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_reference_no), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_time)+curTime, 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_amount)+amount, 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_dotted_line), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_ticket_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_dotted_line), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_cardholder_signature), 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj("\n"));
                add(new PrintItemObj(mContext.getString(R.string.print_confirm_transaction), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj("\n\n"));
            }
        };
    }

    private void getScanPrintFakeString() {
        final String amount = PosApplication.getApp().mConsumeData.getAmount();

        mPrintObjs = new ArrayList<PrintItemObj>() {
            {
                add(new PrintItemObj(mContext.getString(R.string.print_purchase_order), 16, true, PrintItemObj.ALIGN.CENTER));
                add(new PrintItemObj(mContext.getString(R.string.print_transaction_documents), 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_version), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_business_name), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_business_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_terminal_number), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_ticket_number_scan), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_reference_no_scan), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_transaction_type_scan), 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_amount)+amount, 16, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_time)+curTime, 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_no_password_scan), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj(mContext.getString(R.string.print_notice_scan), 8, true, PrintItemObj.ALIGN.LEFT));
                add(new PrintItemObj("\n\n"));
            }
        };

    }

    private PrintItemObj getPrintItemObjs(String printText, int fontSize, PrintItemObj.ALIGN align) {
        PrintItemObj printItemObj = new PrintItemObj(printText, fontSize, false, align);
        return printItemObj;
    }
    /**add by zongli for fake print data */
    public void setCurTime(String curTime) {
        this.curTime = curTime;
    }

    public String getCurTime(){
        return curTime;
    }
    /**add end */
}