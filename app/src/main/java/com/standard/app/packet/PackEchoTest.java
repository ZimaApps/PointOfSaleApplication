package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;

public class PackEchoTest {
	private static final String TAG = PackEchoTest.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;
	
	public PackEchoTest(String termId, String merId) {
        Log.i(TAG, "PackEchoTest()");
        mISO8583 = new ISO8583();
        mISO8583.clearBit();

        String field60 = null;
        
        mISO8583.setBit(0 , PackUtils.MSGTYPEID_ECHOTEST.getBytes(), PackUtils.MSGTYPEID_ECHOTEST.length());
        mISO8583.setBit(41, termId.getBytes(), termId.length());
        mISO8583.setBit(42, merId.getBytes(), merId.length());

        field60 = "00" + Utils.TEST_batchNum + "301";
        mISO8583.setBit(60, field60.getBytes(),field60.length());
        
        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }
	
	public byte[] get()
	{
		return mPacketMsg;
	}
}
