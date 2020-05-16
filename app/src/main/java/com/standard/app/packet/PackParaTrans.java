package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.ISO8583;

public class PackParaTrans {
	private static final String TAG = PackParaTrans.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;
	
	public PackParaTrans(String termId, String merId) {
		Log.i(TAG, "PackParaTrans()");
		mISO8583   = new ISO8583();
        mISO8583.clearBit();

		byte[] field46 = null;
		String field60 = null;
        
        mISO8583.setBit(0 , PackUtils.MSGTYPEID_PARA_TRANS.getBytes(), PackUtils.MSGTYPEID_PARA_TRANS.length());
		mISO8583.setBit(41, termId.getBytes(), termId.length());
		mISO8583.setBit(42, merId.getBytes(), merId.length());

		field46 = getField46("");
		if (field46 != null) {
			mISO8583.setBit(46, field46, field46.length);
		}

		field60 = "06" + Utils.TEST_batchNum + "360";
        mISO8583.setBit(60, field60.getBytes(),field60.length());
        
        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
	}
	
	public byte[] get()
	{
		return mPacketMsg;
	}

	private byte[] getField46(String f46) {
		if(f46.length() > 0) {
			byte[] tmpb = new byte[3 + f46.length()];
			tmpb[0] = (byte) 0x8F;
			tmpb[1] = 0x09;
			tmpb[2] = (byte) f46.length();

			System.arraycopy(f46.getBytes(), 0, tmpb, 3, f46.length());
			return tmpb;
		}
		return null;
	}
}
