package com.standard.app.packet;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.storage.MerchantInfo;
import com.standard.app.util.PacketProcessUtils;

public class PackPacket {
    private static final String TAG = Utils.TAGPUBLIC + PackPacket.class.getSimpleName();

    private Context mContext;
    private String mTermId;
    private String mMerId;
    private String mTradNum;
    private String mTPDU;
    private MerchantInfo mMerchantInfo;

    private PackEmv mPackEmv;
    private byte[] mField62 = null;
    private int mQueryEmvNum = 0;

    public PackPacket(Context context, String tpdu) {
        mContext = context;
        mTPDU = tpdu;

        mMerchantInfo = new MerchantInfo(context);
        mTermId = mMerchantInfo.getTermId();
        mMerId = mMerchantInfo.getMerchantId();
    }

    public byte[] getSendPacket(int procType, Bundle data) {
        Log.i(TAG, "getSendPacket()");
        if (procType == PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT) {
            mTradNum = mMerchantInfo.getTradNum(true);
            PackOnlineInit packOnlineInit = new PackOnlineInit(mTermId, mMerId, mTradNum, data.getInt(PackOnlineInit.PACK_ONLINE_INIT_TIME));
            return packOnlineInit.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_SIGN_UP) {
            mTradNum = mMerchantInfo.getTradNum(true);
            PackSignup packSignup = new PackSignup(mTermId, mMerId, mTradNum);
            return packSignup.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_PARAM_TRANS) {
            PackParaTrans packParaTrans = new PackParaTrans(mTermId, mMerId);
            return packParaTrans.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_STATUS_UPLOAD) {
            PackStatusUpload packStatusUpload = new PackStatusUpload(mTermId, mMerId, mTPDU);
            return packStatusUpload.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_ECHO_TEST) {
            PackEchoTest packEchoTest = new PackEchoTest(mTermId, mMerId);
            return packEchoTest.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD ||
                procType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD) {
            int emvStatus = data.getInt("emv_download_status");
            if (emvStatus == PackEmv.EMV_DOWNLOAD_STATUS_QUERY) {
                mField62 = ("1" + PackUtils.getfixedNumber(mQueryEmvNum++, 2, "0")).getBytes();
                mPackEmv = new PackEmv(mTermId, mMerId, procType, PackEmv.EMV_DOWNLOAD_STATUS_QUERY, mField62);
                return mPackEmv.get();
            } else if (emvStatus == PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD){
                mField62 = data.getByteArray("emv_field_62");
                mPackEmv = new PackEmv(mTermId, mMerId, procType, PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD, mField62);
                return mPackEmv.get();
            } else if (emvStatus == PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD_END) {
                mPackEmv = new PackEmv(mTermId, mMerId, procType, PackEmv.EMV_DOWNLOAD_STATUS_DOWNLOAD_END, null);
                return mPackEmv.get();
            }
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_CONSUME) {
            mTradNum = mMerchantInfo.getTradNum(true);
            PackConsume consume = new PackConsume(mContext, mTermId, mMerId, mTradNum);
            return consume.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_CONSUME_POSITIVE) {
            PackConsumePostive consumePostive = new PackConsumePostive(mContext);
            return consumePostive.get();
        } else if (procType == PacketProcessUtils.PACKET_PROCESS_SCAN) {
            mTradNum = mMerchantInfo.getTradNum(true);
            PackScan packScan = new PackScan(mTermId, mMerId, mTradNum, data);
            return packScan.get();
        }
        return null;
    }
}