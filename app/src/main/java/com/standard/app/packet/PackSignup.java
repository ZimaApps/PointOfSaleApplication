package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;

public class PackSignup {
	private static final String TAG = PackSignup.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;
	
    public PackSignup(String termId, String merId, String tradNum) {
        Log.i(TAG, "PackSignup()");
        mISO8583 = new ISO8583();
        mISO8583.clearBit();

        String field11 = null;
        String field60 = null;
        String field63 = null;

        mISO8583.setBit(0 , PackUtils.MSGTYPEID_LOG_IN.getBytes(), PackUtils.MSGTYPEID_LOG_IN.length());
        mISO8583.setBit(41, termId.getBytes(), termId.length());
        mISO8583.setBit(42, merId.getBytes(), merId.length());

        field11 = tradNum;
        mISO8583.setBit(11, field11.getBytes(), field11.length());

        field60 = "50" + Utils.TEST_batchNum + "003";
        mISO8583.setBit(60, field60.getBytes(), field60.length());

        field63 = Utils.TEST_operatorId+" ";
        mISO8583.setBit(63, field63.getBytes(), field63.length());

        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }

    public byte[] get()
    {
    	return mPacketMsg;
    }
}
