package com.standard.app.card;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.PosApplication;
import com.standard.app.Utils;
import com.standard.app.activity.CardConfirmActivity;
import com.standard.app.activity.PacketProcessActivity;
import com.standard.app.activity.PinpadActivity;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.cache.ConsumeData;
import com.standard.app.paycorpHttp.JsonAndHttpsUtils;
import com.standard.app.util.PacketProcessUtils;
import com.topwise.cloudpos.aidl.emv.AidlPboc;
import com.topwise.cloudpos.aidl.emv.AidlPbocStartListener;
import com.topwise.cloudpos.aidl.emv.CardInfo;
import com.topwise.cloudpos.aidl.emv.PCardLoadLog;
import com.topwise.cloudpos.aidl.emv.PCardTransLog;

import static com.standard.app.cache.Static.FINALICCDATA;

public class ICPbocStartListenerSub extends AidlPbocStartListener.Stub {
    private static final String TAG = Utils.TAGPUBLIC + ICPbocStartListenerSub.class.getSimpleName();

    private Context mContext;
    private AidlPboc mPbocManager;
    private boolean isOnline = false;

    private boolean isGetPin = false;

    public ICPbocStartListenerSub(Context context) {
        mContext = context;
        mPbocManager = DeviceTopUsdkServiceManager.getInstance().getPbocManager();
        CardManager.getInstance().initCardResultCallBack(callBack);
    }

    @Override
    public void finalAidSelect() throws RemoteException {

       /* mPbocManager.setTlv("9f1a","0360".getBytes());
        mPbocManager.setTlv("5f2a","0360".getBytes());
        mPbocManager.setTlv("9f3c","0360".getBytes());*/

        mPbocManager.setTlv("9F1A", BCDASCII.hexStringToBytes("0360"));
        mPbocManager.setTlv("5F2A", BCDASCII.hexStringToBytes("0360"));
        mPbocManager.setTlv("9f3c", BCDASCII.hexStringToBytes("0360"));



        mPbocManager.importFinalAidSelectRes(true);
    }

    /**
     * 请求输入金额 ，简易流程时不回调此方法
     */
    @Override
    public void requestImportAmount(int type) throws RemoteException {
        Log.i(TAG, "requestImportAmount(), type: " + type);

        /*String s = "0840";
        mPbocManager.setTlv("9F1A", BCDASCII.hexStringToBytes(s));
        mPbocManager.setTlv("5F2A", BCDASCII.hexStringToBytes(s));*/

        boolean isSuccess = mPbocManager.importAmount(PosApplication.getApp().mConsumeData.getAmount());
        Log.i(TAG, "IS AMOUNT PRESENT: " + isSuccess + " AMOUNT "+PosApplication.getApp().mConsumeData.getAmount());
//        Log.i(TAG, "SCAN RESULT: " + isSuccess + " RESULT "+PosApplication.getApp().mConsumeData.getScanResult().toString());
        Log.i(TAG, "IC DATA: " + isSuccess + " IC "+PosApplication.getApp().mConsumeData.getICData());
        Log.i(TAG, "PIN: " + isSuccess + " PIN "+PosApplication.getApp().mConsumeData.getPin());
    }

    /**
     * 请求提示信息
     */
    @Override
    public void requestTipsConfirm(String msg) throws RemoteException {
        Log.i(TAG, "requestTipsConfirm(), msg: " + msg);
    }

    /**
     * 请求多应用选择
     */
    @Override
    public void requestAidSelect(int times, String[] aids) throws RemoteException {
        Log.i(TAG, "requestAidSelect(), times: " + times + ", aids.length = " + aids.length);

        boolean isSuccess = mPbocManager.importAidSelectRes(0);
        Log.i(TAG, "isSuccess() : " + isSuccess);
    }

    /**
     * 请求确认是否使用电子现金
     */
    @Override
    public void requestEcashTipsConfirm() throws RemoteException {
        Log.i(TAG, "requestEcashTipsConfirm()");

        boolean isSuccess = mPbocManager.importECashTipConfirmRes(false);
        Log.i(TAG, "isSuccess() : " + isSuccess);
    }

    /**
     * 请求确认卡信息
     */
    @Override
    public void onConfirmCardInfo(CardInfo cardInfo) throws RemoteException {
        String cardno = cardInfo.getCardno();
        Log.i(TAG, "THE CARD NUMBER, cardno: " + cardno);
        Log.i(TAG, "DESCRIPTION, Description: " + cardInfo.describeContents());
        Log.i(TAG, "CARD TYPE, CARD TYPE: " + ConsumeData.CARD_TYPE_IC);

        isEcCard();

        PosApplication.getApp().mConsumeData.setCardType(ConsumeData.CARD_TYPE_IC);
        PosApplication.getApp().mConsumeData.setCardno(cardno);
        CardManager.getInstance().startActivity(mContext, null, CardConfirmActivity.class);
    }

    /**
     * 请求导入PIN
     */
    @Override
    public void requestImportPin(int type, boolean lasttimeFlag, String amt) throws RemoteException {
        Log.i(TAG, "requestImportPin(), type: " + type + "; lasttimeFlag: " + lasttimeFlag + "; amt: " + amt);
        isGetPin = true;
        Bundle param = new Bundle();
        param.putInt("type", type);
        CardManager.getInstance().startActivity(mContext, param, PinpadActivity.class);
    }

    /**
     * 请求身份认证
     */
    @Override
    public void requestUserAuth(int certype, String certnumber) throws RemoteException {
        Log.i(TAG, "requestUserAuth(), certype: " + certype + "; certnumber: " + certnumber);

        boolean isSuccess = mPbocManager.importUserAuthRes(true);
        Log.i(TAG, "isSuccess() : " + isSuccess);
    }

    /**
     * 请求联机
     */
    @Override
    public void onRequestOnline() throws RemoteException {
        Log.i(TAG, "onRequestOnline()");

        setExpired();
        setSeqNum();
        setTrack2();
        setConsume55();
        setConsumePositive55();

        isOnline = true;
        if (!isGetPin) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("online", true);
            CardManager.getInstance().startActivity(mContext, bundle, PinpadActivity.class);
        } else {
            //socket通信
            Bundle bundle = new Bundle();
            bundle.putInt(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_CONSUME);
            CardManager.getInstance().startActivity(mContext, bundle, PacketProcessActivity.class);
        /*byte[] sendData = PosApplication.getApp().mConsumeData.getICData();
        Log.i(TAG, BCDASCII.bytesToHexString(sendData));
        JsonAndHttpsUtils.sendJsonData(mContext, BCDASCII.bytesToHexString(sendData));*/
        }
    }

    /**
     * 返回读取卡片脱机余额结果
     */
    @Override
    public void onReadCardOffLineBalance(String moneyCode, String balance, String secondMoneyCode, String secondBalance) throws RemoteException {
        Log.i(TAG, "onReadCardOffLineBalance(), moneyCode: " + moneyCode + "; balance"
                + "; secondMoneyCode: " + secondMoneyCode + "; secondBalance: " + secondBalance);
    }

    /**
     * 返回读取卡片交易日志结果
     */
    @Override
    public void onReadCardTransLog(PCardTransLog[] log) throws RemoteException {
        Log.i(TAG, "onReadCardTransLog()");
        if (log == null) {
            return;
        }
        Log.i(TAG, "onReadCardTransLog log.length: " + log.length);
    }

    /**
     * 返回读取卡片圈存日志结果
     */
    @Override
    public void onReadCardLoadLog(String atc, String checkCode, PCardLoadLog[] logs) throws RemoteException {
        Log.i(TAG, "onReadCardLoadLog(), atc: " + atc + "; checkCode: " + checkCode + "logs.length: " + logs.length);
        if (logs == null) {
            return;
        }
    }

    /**
     * 交易结果
     * 批准: 0x01
     * 拒绝: 0x02
     * 终止: 0x03
     * FALLBACK: 0x04
     * 采用其他界面: 0x05consume55TlvList
     * 其他：0x06
     * EMV简易流程不回调此方法
     */
    @Override
    public void onTransResult(int result) throws RemoteException {
        Log.i(TAG, "onTransResult resultXXX: " + result + " IS ONLINE "+isOnline);

        //Log.i(TAG, "SCAN DATA resultXXX: " +PosApplication.getApp().mConsumeData.getScanResult().toString());
        //Log.i(TAG, "CARD NUMBER resultXXX: " +PosApplication.getApp().mConsumeData.getCardno().toString());
        if (!isOnline) {
            CardManager.getInstance().callBackTransResult(result);
        }
    }

    @Override
    public void onError(int errorCode) throws RemoteException {
        Log.i(TAG, "onError errorCode: " + errorCode);
        CardManager.getInstance().callBackError(errorCode);
    }

    CardManager.CardResultCallBack callBack = new CardManager.CardResultCallBack() {
        @Override
        public void consumeAmount(String amount) {
            Log.i(TAG, "consumeAmount amount : " + amount);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importAmount(amount);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void aidSelect(int index) {
            Log.i(TAG, "aidSelect index : " + index);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importAidSelectRes(index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void eCashTipsConfirm(boolean confirm) {
            Log.i(TAG, "eCashTipsConfirm confirm : " + confirm);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importECashTipConfirmRes(confirm);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void confirmCardInfo(boolean confirm) {
            Log.i(TAG, "confirmCardInfo confirm : " + confirm);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importConfirmCardInfoRes(confirm);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void importPin(String pin) {
            Log.i(TAG, "importPin pin : " + pin);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importPin(pin);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void userAuth(boolean auth) {
            Log.i(TAG, "userAuth auth : " + auth);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importUserAuthRes(auth);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void requestOnline(boolean online, String respCode, String icc55) {
            Log.i(TAG, "DATA MUHIIIMU ICC : " + online + " respCode : " + respCode + " icc55 : " + icc55);
            if (null != mPbocManager) {
                try {
                    mPbocManager.importOnlineResp(online, respCode, icc55);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean isEcCard() {
        Log.i(TAG, "isEcCard()");
        String ecCard = "A000000333010106";

        String[] aidTag = new String[]{"84"};
        String cardAid = BCDASCII.bytesToHexString(getTlv(aidTag));

        Log.i(TAG, "cardAid: " + cardAid);
        return cardAid.contains(ecCard);
    }

    private void setExpired() {
        Log.i(TAG, "getExpired()");
        String[] dataTag = new String[]{"5F24"};
        byte[] dataTlvList = getTlv(dataTag);
        String expired = null;

        if (dataTlvList != null) {
            expired = BCDASCII.bytesToHexString(dataTlvList);
            expired = expired.substring(expired.length() - 6, expired.length() - 2);
        }
        Log.i(TAG, "setExpired : " + expired);
        PosApplication.getApp().mConsumeData.setExpiryData(expired);
    }

    private void setSeqNum() {
        Log.i(TAG, "getSeqNum()");
        String[] seqNumTag = new String[]{"5F34"};
        byte[] seqNumTlvList = getTlv(seqNumTag);
        String cardSeqNum = null;

        if (seqNumTlvList != null) {
            cardSeqNum = BCDASCII.bytesToHexString(seqNumTlvList);
            cardSeqNum = cardSeqNum.substring(cardSeqNum.length() - 2, cardSeqNum.length());
        }
        Log.i(TAG, "setSeqNum : " + cardSeqNum);
        PosApplication.getApp().mConsumeData.setSerialNum(cardSeqNum);
    }

    private void setTrack2() {
        Log.i(TAG, "getTrack2()");
        String[] track2Tag = new String[]{"57"};
        byte[] track2TlvList = getTlv(track2Tag);

        byte[] temp = new byte[track2TlvList.length - 2];
        System.arraycopy(track2TlvList, 2, temp, 0, temp.length);
        String track2 = processTrack2(BCDASCII.bytesToHexString(temp));
        PosApplication.getApp().mConsumeData.setSecondTrackData(track2);
    }

    private static String processTrack2(String track) {
        Log.i(TAG, "processTrack2()");
        StringBuilder builder = new StringBuilder();
        String subStr = null;
        String resultStr = null;
        for (int i = 0; i < track.length(); i++) {
            subStr = track.substring(i, i + 1);
            if (!subStr.endsWith("F")) {
                /*if(subStr.endsWith("D")) {
                    builder.append("=");
                } else {*/
                builder.append(subStr);
                /*}*/
            }
        }
        resultStr = builder.toString();
        return resultStr;
    }

    private void setConsume55() {
        Log.i(TAG, "getConsume55() THE ICC STRING");
        /*String[] consume55Tag = new String[]{"9F26", "9F27", "9F10", "9F37", "9F36", "95", "9A", "9C", "9F02", "5F2A",
                "82", "9F1A", "9F03", "9F33", "9F34", "9F35", "9F1E", "84", "9F09",
                "91", "71", "72", "DF32", "DF33", "DF34"};*/
        String[] consume55Tag = new String[]{"4F", "82", "95", "9A", "9B", "9C", "5F24",
                "5F2A", "9F02", "9F03", "9F06", "9F10", "9F12", "9F1A", "9F1C", "9F26",
                "9F27", "9F33", "9F34", "9F36", "9F37", "C2", "CD", "CE", "C0", "C4",
                "C7", "C8"};
        byte[] consume55TlvList = getTlv(consume55Tag);
        FINALICCDATA = BCDASCII.bytesToHexString(consume55TlvList);
        Log.i(TAG, "setConsume55 consume55TlvList : " + BCDASCII.bytesToHexString(consume55TlvList));
        PosApplication.getApp().mConsumeData.setICData(consume55TlvList);
    }

    private void setConsumePositive55() {
        Log.i(TAG, "getConsumePositive55()");
        String[] postive55Tag = new String[]{"95", "9F1E", "9F10", "9F36"};
        byte[] postive55TagTlvList = getTlv(postive55Tag);
        Log.i(TAG, "setConsume55 postive55TagTlvList : " + BCDASCII.bytesToHexString(postive55TagTlvList));
        PosApplication.getApp().mConsumeData.setICPositiveData(postive55TagTlvList);
    }

    private byte[] getTlv(String[] tags) {
        byte[] tempList = new byte[500];
        byte[] tlvList = null;
        try {
            for (String tag : tags) {
                String[] tempStr = {tag};
                byte[] tempByte = new byte[500];
                int len = mPbocManager.readKernelData(tempStr, tempByte);
                Log.i(TAG, "MAY BE TAGS temp: " + BCDASCII.bytesToHexString(tempByte, len));
            }

            int result = mPbocManager.readKernelData(tags, tempList);

            if (result < 0) {
                return null;
            } else {
                tlvList = new byte[result];
                System.arraycopy(tempList, 0, tlvList, 0, result);
                Log.i(TAG, "HIZI NINI tlvList: " + BCDASCII.bytesToHexString(tlvList));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return tlvList;
    }
}