package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.util.PacketProcessUtils;

public class PackEmv {
    private static final String TAG = PackEmv.class.getSimpleName();

    public static final int EMV_DOWNLOAD_STATUS_QUERY = 101;
    public static final int EMV_DOWNLOAD_STATUS_DOWNLOAD = 102;
    public static final int EMV_DOWNLOAD_STATUS_DOWNLOAD_END = 103;

    private ISO8583 mISO8583;
    private byte[] mPacketMsg = null;

    public PackEmv(String termId, String merId, int packetType, int procType,  byte[] f62) {
        Log.i(TAG, "PackEmv()");
        mISO8583   = new ISO8583();
        mISO8583.clearBit();

        String field0 = null;
        String field60 = null;
        String field63 = null;

        if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_QUERY) {
            field0 = PackUtils.MSGTYPEID_EMV_QUERY;
            field60 = "01" + Utils.TEST_batchNum + "372";
            field63 = "100";
        } else if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_DOWNLOAD) {
            field0 = PackUtils.MSGTYPEID_EMV_DOWNLOAD;
            field60 = "01" + Utils.TEST_batchNum + "370";
            field63 = null;
        } else if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_CAPK_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_DOWNLOAD_END) {
            field0 = PackUtils.MSGTYPEID_EMV_DOWNLOAD_END;
            field60 = "01" + Utils.TEST_batchNum + "371";
            field63 = null;
        } else if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_QUERY) {
            field0 = PackUtils.MSGTYPEID_ECHOTEST;
            field60 = "01" + Utils.TEST_batchNum + "382";
            field63 = "100";
        } else if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_DOWNLOAD) {
            field0 = PackUtils.MSGTYPEID_EMV_DOWNLOAD;
            field60 = "01" + Utils.TEST_batchNum + "380";
            field63 = null;
        } else if (packetType == PacketProcessUtils.PACKET_PROCESS_IC_PARA_DOWNLOAD && procType == EMV_DOWNLOAD_STATUS_DOWNLOAD_END) {
            field0 = PackUtils.MSGTYPEID_EMV_DOWNLOAD_END;
            field60 = "01" + Utils.TEST_batchNum + "381";
            field63 = null;
        }

        Log.i(TAG, "field0 = "+field0);
        Log.i(TAG, "field60 = "+field60);
        Log.i(TAG, "field63 = "+field63);

        mISO8583.setBit(0 , field0.getBytes(), field0.length());
        mISO8583.setBit(41, termId.getBytes(), termId.length());
        mISO8583.setBit(42, merId.getBytes(), merId.length());

        if (field60 != null) {
            mISO8583.setBit(60, field60.getBytes(), field60.length());
        }
        if (f62 != null) {
            mISO8583.setBit(62, f62, f62.length);
        }
        if (field63 != null) {
            mISO8583.setBit(63, field63.getBytes(), field63.length());
        }

        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }

    public byte[] get()
    {
        return mPacketMsg;
    }
}
