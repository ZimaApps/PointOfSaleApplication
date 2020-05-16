package com.standard.app.packet;

import android.os.Bundle;
import android.util.Log;

import com.standard.app.PosApplication;
import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.iso8583.ISO8583Util;

/**
 * Created by topwise on 17-7-18.
 */
public class PackScan {
    private static final String TAG = Utils.TAGPUBLIC +  PackScan.class.getSimpleName();

    private ISO8583 mISO8583;
    private byte[] mPacketMsg = null;

    public PackScan(String termId, String merId, String tradNum, Bundle data) {
        Log.i(TAG, "PackScan()");
        mISO8583 = new ISO8583();
        mISO8583.clearBit();

        String field3 = null;
        String field4 = null;
        String field11 = null;
        String field22 = null;
        String field25 = null;
        byte[] field46 = null;
        byte[] field64 = null;

        mISO8583.setBit(0, PackUtils.MSGTYPEID_SCAN.getBytes(), PackUtils.MSGTYPEID_SCAN.length());

        field3 = "400100";
        mISO8583.setBit(3, field3.getBytes(), field3.length());

        String amount = PosApplication.getApp().mConsumeData.getAmount();
        field4 = PackUtils.getField4(amount);
        mISO8583.setBit(4, field4.getBytes(), field4.length());

        field11 = tradNum;
        mISO8583.setBit(11, field11.getBytes(), field11.length());
        Log.i(TAG, "11 = " + field11);

        field22 = "0300";
        mISO8583.setBit(22, field22.getBytes(), field22.length());

        field25 = "00";
        mISO8583.setBit(25, field25.getBytes(), field25.length());

        mISO8583.setBit(41, termId.getBytes(), termId.length());

        mISO8583.setBit(42, merId.getBytes(), merId.length());

        field46 = getField46(PosApplication.getApp().mConsumeData.getScanResult());
        mISO8583.setBit(46, field46, field46.length);

        String field60 = "52" + Utils.TEST_batchNum + "000" +  "000" ;
        mISO8583.setBit(60, field60.getBytes(), field60.length());

        field64 = PackUtils.getField64(mISO8583);
        mISO8583.setBit(64, field64, field64.length);

        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }


    public byte[] get() {
        return mPacketMsg;
    }

    private byte[] getField46(String code) {
        String tag = "5F52";
        byte[] bcdtag =  BCDASCII.fromASCIIToBCD(tag.getBytes(), 0, tag.length(), false);
        String value = "3133023002";
        byte[] bcdvalue = code.getBytes();
        byte[] stx ={0x02};
        byte[] bcdvalue1 = BCDASCII.fromASCIIToBCD(value.getBytes(), 0, value.length(), false);
        int length = bcdvalue.length+bcdvalue1.length;
        byte [] temp1 = ISO8583Util.byteArrayAdd(bcdvalue1,bcdvalue,stx);
        String asclen = new String().format("%02X", length+1);
        byte[] bcdlength = BCDASCII.fromASCIIToBCD(asclen.getBytes(), 0, asclen.length(), false);
        return ISO8583Util.byteArrayAdd(bcdtag, bcdlength, temp1);
    }
}
