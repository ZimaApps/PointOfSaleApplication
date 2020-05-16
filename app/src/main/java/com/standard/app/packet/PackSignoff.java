package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;

public class PackSignoff {
    private static final String TAG = PackSignoff.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[]  mPacketMsg = null;
	
    public PackSignoff(String termId, String merId, String tradNum) {
        Log.i(TAG, "PackSignoff()");
        mISO8583 = new ISO8583();
        mISO8583.clearBit();

        String field11 = null;
        String field60 = null;

        mISO8583.setBit(0 , PackUtils.MSGTYPEID_LOG_OFF.getBytes(), PackUtils.MSGTYPEID_LOG_OFF.length());
        mISO8583.setBit(41, termId.getBytes(), termId.length());
        mISO8583.setBit(42, merId.getBytes(), merId.length());

        field11 = tradNum;
        mISO8583.setBit(11, field11.getBytes(), field11.length());

        field60 = "50" + Utils.TEST_batchNum + "003";
        mISO8583.setBit(60, field60.getBytes(), field60.length());

        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }

    public byte[] get()
    {
    	return mPacketMsg;
    }
}
