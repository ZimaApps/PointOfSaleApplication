package com.standard.app.packet;

import android.content.Context;
import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.storage.ConsumeFieldInfo;
import com.standard.app.storage.ConsumeFieldUtils;

public class PackConsumePostive {
	private static final String TAG = Utils.TAGPUBLIC + PackConsumePostive.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;

	public PackConsumePostive(Context context) {
		ConsumeFieldInfo consumeFieldInfo = new ConsumeFieldInfo(context);
		mISO8583 = new ISO8583();
		mISO8583.clearBit();

		String field2 = null;
		String field3 = null;
		String field4 = null;
		String field11 = null;
		String field14 = null;
		String field22 = null;
		String field23 = null;
		String field25 = null;
		String field35 = null;
		String field36 = null;
		String field39 = null;
		String field41 = null;
		String field42 = null;
		byte[] field46 = null;
		String field49 = null;
		byte[] field55 = null;
		String field60 = null;
		byte[] field64 = null;

		mISO8583.setBit(0, PackUtils.MSGTYPEID_POSTIVE.getBytes(), PackUtils.MSGTYPEID_POSTIVE.length());

		field41 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD41);
		mISO8583.setBit(41, field41.getBytes(), field41.length());
		field42 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD42);
		mISO8583.setBit(42, field42.getBytes(),field42.length());

		field2 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD2);
		if(field2 != null){
			mISO8583.setBit(2, field2.getBytes(), field2.length());
			Log.d(TAG, "2 = " + field2);
		}

		field3 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD3);
		mISO8583.setBit(3, field3.getBytes(), field3.length());
		Log.d(TAG, "3 = " + field3);

		field4 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD4);
		mISO8583.setBit(4, field4.getBytes(), field4.length());
		Log.d(TAG, "4 = " + field4);

		field11 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD11);
		mISO8583.setBit(11, field11.getBytes(), field11.length());
		Log.d(TAG, "11 = " + field11);

		field14 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD14);
		if (field14 != null) {
			mISO8583.setBit(14, field14.getBytes(), field14.length());
			Log.d(TAG, "14 = " + field14);
		}

		field22 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD22);
		mISO8583.setBit(22, field22.getBytes(), field22.length());
		Log.d(TAG, "22 = " + field22);

		field23 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD23);
		if (field23 != null) {
			mISO8583.setBit(23, field23.getBytes(), field23.length());
		}

		field25 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD25);
		mISO8583.setBit(25, field25.getBytes(), field25.length());
		Log.d(TAG, "25 = " + field25);

		field35 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD35);
		if (field35 != null) {
			mISO8583.setBit(35, field35.getBytes(), 2 * field35.length());
			Log.d(TAG, "35 = " + field35);

		}

		field36 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD36);
		if(field36!=null) {
			mISO8583.setBit(36, field36.getBytes(),field36.length());
			Log.d(TAG, "36 = " + field36);
		}

		field39 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD39);
		mISO8583.setBit(39, field39.getBytes(), field39.length());
		Log.d(TAG, "39 = " + field39);

		field46 = BCDASCII.hexStringToBytes(consumeFieldInfo.getField(ConsumeFieldUtils.FIELD46));
		mISO8583.setBit(46, field46, field46.length);

		field49 = consumeFieldInfo.getField(ConsumeFieldUtils.FIELD49);
		if(field49 != null){
			mISO8583.setBit(49, field49.getBytes(), field49.length());
			Log.d(TAG, "49 = " + field49);
		}

		field55 = BCDASCII.hexStringToBytes(consumeFieldInfo.getField(ConsumeFieldUtils.FIELD55));
		if (field55 != null) {
			mISO8583.setBit(55, field55, field55.length);
			Log.d(TAG, "55 = " + field55);
		}

		field60 = "22" + Utils.TEST_batchNum + "000" + consumeFieldInfo.getField(ConsumeFieldUtils.FIELD604) + "0"+ "0000" + "00";
		mISO8583.setBit(60, field60.getBytes(), field60.length());
		Log.d(TAG, "60 = " + field60);

		String batchNo = consumeFieldInfo.getField(ConsumeFieldUtils.BATCH_NO);
		String date = consumeFieldInfo.getField(ConsumeFieldUtils.DATA);
		String field61 = batchNo + field11 + date;
		mISO8583.setBit(61, field61.getBytes(),field61.length());
		Log.d(TAG, "61 = " + field61);

		field64 = PackUtils.getField64(mISO8583);
		mISO8583.setBit(64, field64, field64.length);

		byte[] isobyte = mISO8583.isotostr();
		mPacketMsg = PackUtils.getPacketHeader(isobyte, field41, field42);
	}

	public byte[] get() {
		return mPacketMsg;
	}
}
