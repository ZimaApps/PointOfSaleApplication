package com.standard.app.packet;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.activity.PacketProcessActivity;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583Util;
import com.standard.app.storage.MerchantInfo;
import com.standard.app.util.PacketProcessUtils;
import com.standard.app.util.TLVDecode;
import com.topwise.cloudpos.aidl.emv.AidlPboc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnpackPacket {
    private static final String TAG = Utils.TAGPUBLIC + UnpackPacket.class.getSimpleName();

    private MerchantInfo mMerchantInfo;
    private String mTermId;
    private byte[] mRecePacket = null;
    private int mProcType;
    private String mResponse;
    private String mResponseDetail;
    private byte[] mField47;

    private UnpackOnlineInit mUnpackOnlineInit;
    private UnpackEmv mUnpackEmv;

    private AidlPboc mPobcManager;
    private List<LinkedHashMap<byte[], byte[]>> mQueryEmvList;
    private ArrayList<byte[]> mDownload62List;

    private byte[] mFieldRece62 = null;
    private int mField62FirstByte;
    private int mDownloacEmvNum = 0;
    private int mSetEmvNum = 0;

    public UnpackPacket(Context context, int procType) {
        mProcType = procType;

        mMerchantInfo = new MerchantInfo(context);
        mTermId = mMerchantInfo.getTermId();
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            mUnpackOnlineInit = new UnpackOnlineInit();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD ||
                mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            mQueryEmvList = new ArrayList<LinkedHashMap<byte[], byte[]>>();
            mPobcManager = DeviceTopUsdkServiceManager.getInstance().getPbocManager();
        }
    }

    public void procRecePacket(Context context, byte[] recePacket, Bundle data) {
        Log.i(TAG, "procRecePacket(), mProcType" + mProcType);
        mRecePacket = recePacket;

        if (mProcType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            mUnpackOnlineInit.onlineInit(mTermId, data.getInt(PackOnlineInit.PACK_ONLINE_INIT_TIME), mRecePacket, mRecePacket.length);
            mResponse = mUnpackOnlineInit.getResponse();
            mResponseDetail = mUnpackOnlineInit.getResponseDetail();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP) {
            UnpackSignup unpackSignup = new UnpackSignup(mRecePacket, mRecePacket.length);
            mResponse = unpackSignup.getResponse();
            mResponseDetail = unpackSignup.getResponseDetail();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS) {
            UnpackParaTrans unpackParaTrans = new UnpackParaTrans(context, mRecePacket, mRecePacket.length);
            mResponse = unpackParaTrans.getResponse();
            mResponseDetail = unpackParaTrans.getResponseDetail();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD ||
                mProcType == PacketProcessUtils.PACKET_PROCESS_ECHO_TEST ||
                mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME_POSITIVE) {
            UnpackDefault unpackDefault = new UnpackDefault(mRecePacket, mRecePacket.length);
            mResponse = unpackDefault.getResponse();
            mResponseDetail = unpackDefault.getResponseDetail();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD ||
                mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            mUnpackEmv = new UnpackEmv(mRecePacket, mRecePacket.length);
            mResponse = mUnpackEmv.getResponse();
            mResponseDetail = mUnpackEmv.getResponseDetail();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
            UnpackConsume unpackConsume = new UnpackConsume(mRecePacket, mRecePacket.length);
            mResponse = unpackConsume.getResponse();
            mResponseDetail = unpackConsume.getResponseDetail();
            mField47 = unpackConsume.getField47();
        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_SCAN) {
            UnpackScan unpackScan = new UnpackScan(mRecePacket, mRecePacket.length);
            mResponse = unpackScan.getResponse();
            mResponseDetail = unpackScan.getResponseDetail();
            mField47 = unpackScan.getField47();
        }
    }

    public String getResponse() {
        return mResponse;
    }

    public String getResponseDetail() {
        return mResponseDetail;
    }

    public byte[] getField47() {
        return mField47;
    }

    public int procDownloadEmv(int emvStatus) {
        if (emvStatus == PackEmv.EMV_DOWNLOAD_STATUS_QUERY) {
            mFieldRece62 = mUnpackEmv.getField62();
            Log.i(TAG, "query mFieldRece62 == null -->" + (mFieldRece62 == null));
            if (mFieldRece62 != null) {
                mField62FirstByte = mUnpackEmv.getField62FirstByte();
                Log.i(TAG, "query field62 first string is ：" + mField62FirstByte);
                if (mField62FirstByte == '0') {
                    Log.i(TAG, "query field62 first string is 0");
                    return -1;
                } else {
                    mQueryEmvList.add(mDownloacEmvNum++, TLVDecode.getDecodeTLV(mFieldRece62));
                }

                if (mField62FirstByte != '2') {
                    mDownload62List = new ArrayList<byte[]>();
                    for (LinkedHashMap<byte[], byte[]> itemMap : mQueryEmvList) {
                        if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
                            getDownloadCapkTLV62(itemMap);
                        } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
                            getDownloadParaTLV62(itemMap);
                        }
                    }
                    mDownloacEmvNum = 0;
                    Log.i(TAG, "--------->need to download size is : " + mDownload62List.size());
                    return PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD;
                }
            }
            return emvStatus;
        } else if (emvStatus == PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD) {
            mFieldRece62 = mUnpackEmv.getField62();
            Log.i(TAG, "download mFieldRece62 == null -->" + (mFieldRece62 == null));
            if (mFieldRece62 != null) {
                mField62FirstByte = mUnpackEmv.getField62FirstByte();
                Log.i(TAG, "download field62 first string is ：" + mField62FirstByte);
                if (mField62FirstByte == '0') {
                    Log.i(TAG, "download field62 first string is 0");
                } else {
                    if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
                        setEmvCAPK(mFieldRece62);
                    } else if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
                        setEmvPARA(mFieldRece62);
                    }
                }
                if (mDownloacEmvNum == (mDownload62List.size())) {
                    Log.i(TAG, "--------->mSetEmvNum: " + mSetEmvNum);
                    return PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD_END;
                }
            }
            return emvStatus;
        }
        return -1;
    }

    private boolean setEmvCAPK(byte[] f62) {
        if (mPobcManager == null) {
            return false;
        }

        boolean isSetCapkSuccess = false;
        try {
            isSetCapkSuccess = mPobcManager.updateCAPK(0x01, BCDASCII.bytesToHexString(f62));
            Log.i(TAG, "setEmvCAPK: " + isSetCapkSuccess + ", f62: " + BCDASCII.bytesToHexString(f62));
            if (isSetCapkSuccess) {
                mSetEmvNum++;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isSetCapkSuccess;
    }

    public boolean clearEmvCapk() {
        Log.i(TAG, "clearEmvCapk()");
        if (mPobcManager == null) {
            return false;
        }
        boolean isClearCapkSuccess = false;
        try {
            isClearCapkSuccess = mPobcManager.updateCAPK(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isClearCapkSuccess;
    }

    private boolean setEmvPARA(byte[] f62) {
        if (mPobcManager == null) {
            return false;
        }
        boolean isSetParaSuccess = false;
        try {
            isSetParaSuccess = mPobcManager.updateAID(0x01, BCDASCII.bytesToHexString(f62));
            Log.i(TAG, "setEmvPARA: " + isSetParaSuccess + ", f62: " + BCDASCII.bytesToHexString(f62));
            if (isSetParaSuccess) {
                mSetEmvNum++;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isSetParaSuccess;
    }

    public boolean clearEmvPara() {
        Log.i(TAG, "clearEmvPara()");
        if (mPobcManager == null) {
            return false;
        }

        boolean isClearAidSuccess = false;
        try {
            isClearAidSuccess = mPobcManager.updateAID(0x03, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isClearAidSuccess;
    }

    private void getDownloadCapkTLV62(Map<byte[], byte[]> queryCapkList) {
        if (queryCapkList.size() < 1) {
            return;
        }

        byte[] tlv;
        int tlvLen = 0;

        byte[] rid = null;
        byte[] ind = null;
        for (byte[] key : queryCapkList.keySet()) {
            Log.i(TAG, "getDownloadCapkTLV62, key:" + BCDASCII.bytesToHexString(key));

            String tag = BCDASCII.bytesToHexString(key);
            byte[] value;

            value = queryCapkList.get(key);
            int len = PackUtils.getfixedNumber(value.length, 2, "0").length() + value.length;
            switch (tag) {
                case "9F06": // RID
                    rid = new byte[len + 2];
                    rid = ISO8583Util.byteArrayAdd(new byte[]{(byte) 0x9F, 0x06}, BCDASCII.hexStringToBytes(PackUtils.getfixedNumber(value.length, 2, "0")), value);
                    tlvLen += (len + 2);
                    Log.i(TAG, "rid: " + BCDASCII.bytesToHexString(rid));
                    break;
                case "9F22": // IDX
                    ind = new byte[len + 2];
                    ind = ISO8583Util.byteArrayAdd(new byte[]{(byte) 0x9F, 0x22}, BCDASCII.hexStringToBytes(PackUtils.getfixedNumber(value.length, 2, "0")), value);
                    tlvLen += (len + 2);
                    Log.i(TAG, "ind: " + BCDASCII.bytesToHexString(ind));
                    break;
                case "DF05": // EXP DATE
                    break;
                case "DF02": // 公钥模
                    break;
                case "DF04": // 公钥指数
                    break;
                case "DF03": // SHA
                    break;
                default:
                    break;
            }

            if ((rid != null) && (ind != null)) {
                tlv = new byte[tlvLen];
                tlv = ISO8583Util.byteArrayAdd(rid, ind);
                Log.i(TAG, "tlv: " + BCDASCII.bytesToHexString(tlv));
                mDownload62List.add(tlv);
                rid = null;
                ind = null;
            }
        }
    }

    private void getDownloadParaTLV62(Map<byte[], byte[]> queryParaList) {
        if (queryParaList.size() < 1) {
            return;
        }

        byte[] aid = null;
        for (byte[] key : queryParaList.keySet()) {
            Log.i(TAG, "getDownloadParaTLV62, key:" + BCDASCII.bytesToHexString(key));

            String tag = BCDASCII.bytesToHexString(key);
            byte[] value;

            value = queryParaList.get(key);
            int len = PackUtils.getfixedNumber(value.length, 2, "0").length() + value.length;
            switch (tag) {
                case "9F06": // AID
                    aid = new byte[len + 2];
                    aid = ISO8583Util.byteArrayAdd(new byte[]{(byte) 0x9F, 0x06}, BCDASCII.hexStringToBytes(PackUtils.getfixedNumber(value.length, 2, "0")), value);
                    Log.i(TAG, "aid: " + BCDASCII.bytesToHexString(aid));
                    break;
                case "9F1B": // 最低限额
                    break;
                case "9F08": // 应用版本号(卡片),没用吧??银联POSP送这个是错的，应该改为9F09
                case "9F09": // 应用版本号(终端),银联POSP送没有送这个
                    break;
                case "DF01": // ASI,是否允许AID部分匹配
                    break;
                case "DF11": // TAC缺省
                    break;
                case "DF12": // TAC联机
                    break;
                case "DF13": // TAC拒绝
                    break;
                case "DF15": // 偏置随机选择阈值
                    break;
                case "DF16": // 随机选择最大%
                    break;
                case "DF17": // 选择目标%
                    break;
                case "DF14": // 缺省DDOL
                    break;
                case "DF19":
                    break;
                case "DF20":
                    break;
                case "DF21":
                    break;
                case "DF18": // 联机PIN支持能力 1:支持;0不支持
                    break;
                case "9F7B":
                    break;
                default:
                    break;
            }
            if (aid != null) {
                mDownload62List.add(aid);
                aid = null;
            }
        }
    }

    public byte[] getField62(PacketProcessActivity activity) {
        if (mDownload62List == null) {
            return null;
        }
        if (mProcType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD) {
            activity.mTextProcDetail.setText(String.format(activity.getResources().getString(R.string.socket_proc_detail_emv_capk_download), mDownloacEmvNum + 1));
        } else {
            activity.mTextProcDetail.setText(String.format(activity.getResources().getString(R.string.socket_proc_detail_emv_para_download), mDownloacEmvNum + 1));
        }
        byte[] field62 = mDownload62List.get(mDownloacEmvNum++);
        Log.i(TAG, "field62 = " + BCDASCII.bytesToHexString(field62));
        return field62;
    }
}