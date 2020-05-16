package com.standard.app.packet;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.standard.app.PosApplication;
import com.standard.app.Utils;
import com.standard.app.cache.ConsumeData;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.iso8583.ISO8583Util;
import com.standard.app.storage.ConsumeFieldInfo;
import com.standard.app.storage.ConsumeFieldUtils;

public class PackConsume {
	private static final String TAG = Utils.TAGPUBLIC + PackConsume.class.getSimpleName();
	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;

	public PackConsume(Context context, String termId, String merId, String tradNo) {

		ConsumeFieldInfo consumeFieldInfo = new ConsumeFieldInfo(context);
		ConsumeData consumeData = PosApplication.getApp().mConsumeData;

		mISO8583 = new ISO8583();
		mISO8583.clearBit();

		int cardType;
		byte[] field2 = null;
		String field3 = null;
		String field4 = null;
		String field14 = null;
		String field22 = null;
		String field23 = null;
		String field25 = null;
		String field26 = null;
		byte[] field35 = null;
		byte[] field36 = null;
		byte[] field46 = null;
		String field49 = null;
		byte[] field52 = null;
		String field53 = null;
		byte[] field55 = null;
		String field60 = null;
		byte[] field64 = null;

		cardType = consumeData.getCardType();
		field52 = consumeData.getPin();

		mISO8583.setBit(0, PackUtils.MSGTYPEID_CONSUME.getBytes(), PackUtils.MSGTYPEID_CONSUME.length());

		mISO8583.setBit(41, termId.getBytes(), termId.length());
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD41, termId);

		mISO8583.setBit(42, merId.getBytes(), merId.length());
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD42, merId);

		String cardNum = consumeData.getCardno();
		if (cardNum != null) {
			field2 = PackUtils.getTrackField(cardNum, ISO8583.LLVAR_LEN);
			mISO8583.setBit(2, BCDASCII.bytesToHexString(field2).getBytes(), field2.length * 2);
			Log.i(TAG, "2 = " + BCDASCII.bytesToHexString(field2));
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD2, BCDASCII.bytesToHexString(field2));
		}

		field3 = "009000";
		mISO8583.setBit(3, field3.getBytes(), field3.length());
		Log.i(TAG, "3 = " + field3);
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD3, field3);

		String amount = consumeData.getAmount();
		field4 = PackUtils.getField4(amount);
		mISO8583.setBit(4, field4.getBytes(), field4.length());
		Log.i(TAG, "4 = " + field4);
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD4, field4);

		mISO8583.setBit(11, tradNo.getBytes(), tradNo.length());
		Log.i(TAG, "11 = " + tradNo);
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD11, tradNo);

		field14 = consumeData.getExpiryData();
		if (field14 != null) {
			mISO8583.setBit(14, field14.getBytes(), field14.length());
			Log.d(TAG, "14 = " + field14);
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD14, field14);
		}

		if (cardType == ConsumeData.CARD_TYPE_MAG) {
			if (field52 != null) {
				field22 = "021";
			} else {
				field22 = "022";
			}
		} else if (cardType == ConsumeData.CARD_TYPE_IC) {
			if (field52 != null) {
				field22 = "051";
			} else {
				field22 = "052";
			}
		} else if (cardType == ConsumeData.CARD_TYPE_RF) {
			if (field52 != null) {
				field22 = "071";
			} else {
				field22 = "072";
			}
		}
		mISO8583.setBit(22, field22.getBytes(), field22.length());
		Log.d(TAG, "22 = " + field22);
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD22, field22);

		field23 = consumeData.getSerialNum();
		if (field23 != null) {
			mISO8583.setBit(23, field23.getBytes(), field23.length());
			Log.d(TAG, "23 = " + field23);
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD23, field23);
		}

		field25 = "00";
		mISO8583.setBit(25, field25.getBytes(), field25.length());
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD25, field25);

		if (field52 != null) {
			field26 = "06";
			mISO8583.setBit(26, field26.getBytes(), field26.length());
			Log.d(TAG, "26 = " + field26);
		}

		String track2value = consumeData.getSecondTrackData();
		Log.i(TAG, "track2value = "+track2value);
		if (track2value != null && track2value.length() > 0) {
			field35 = PackUtils.getTrackField(track2value, ISO8583.LLVAR_LEN);
			mISO8583.setBit(35, BCDASCII.bytesToHexString(field35).getBytes(), field35.length * 2);
			Log.i(TAG, "35 = " + BCDASCII.bytesToHexString(field35));
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD35, BCDASCII.bytesToHexString(field35));
		}

		String track3value = consumeData.getThirdTrackData();
		Log.i(TAG, "track3value = "+track3value);
		/*if (track3value != null && track3value.length() > 0) {
			field36 = PackUtils.getTrackField(track3value, ISO8583.LLLVAR_LEN);
			mISO8583.setBit(36, BCDASCII.bytesToHexString(field35).getBytes(), field36.length * 2);
			Log.d(TAG, "36 = " + BCDASCII.bytesToHexString(field36));
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD36, BCDASCII.bytesToHexString(field36));
		}*/

		field46 = getField46();
		mISO8583.setBit(46, field46, field46.length);
		Log.d(TAG, "46 = " + BCDASCII.bytesToHexString(field46));
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD46, BCDASCII.bytesToHexString(field46));

		field49 = "156";
		mISO8583.setBit(49, field49.getBytes(), field49.length());
		consumeFieldInfo.setField(ConsumeFieldUtils.FIELD49, field49);

		if (field52 != null) {
			mISO8583.setBit(52, field52, field52.length);
			Log.d(TAG, "52 = " + BCDASCII.bytesToHexString(field52));
		}

		if (field52 == null) {
			field53 = "06000000";
		} else {
			field53 = "26000000";
		}
		if (field53 != null) {
			mISO8583.setBit(53, field53.getBytes(), field53.length());
			Log.d(TAG, "53 = " + field53);
		}

		field55 = consumeData.getICData();
		if (field55 != null) {
			mISO8583.setBit(55, field55, field55.length);
			Log.d(TAG, "55 = " + BCDASCII.bytesToHexString(field55));
		}

		if (cardNum == null) {
			field60 = "22" + Utils.TEST_batchNum + "000" + "5" + "0" + "0000" + "00";
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD604, "5");
		} else {
			field60 = "22" + Utils.TEST_batchNum + "000" + "6" + "0" + "0000" + "00";
			consumeFieldInfo.setField(ConsumeFieldUtils.FIELD604, "6");
		}
		mISO8583.setBit(60, field60.getBytes(), field60.length());
		Log.d(TAG, "60 = " + field60);
		consumeFieldInfo.setField(ConsumeFieldUtils.BATCH_NO, Utils.TEST_batchNum);
		consumeFieldInfo.setField(ConsumeFieldUtils.DATA, getMonthDay());

		field64 = PackUtils.getField64(mISO8583);
		mISO8583.setBit(64, field64, field64.length);
		Log.d(TAG, "64 = " + BCDASCII.bytesToHexString(field64));

		byte[] isobyte = mISO8583.isotostr();
		mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
	}

	public byte[] get() {
		return mPacketMsg;
	}

	private byte[] getField46() {
		String tag = "5F52";
		byte[] bcdtag =  BCDASCII.fromASCIIToBCD(tag.getBytes(), 0, tag.length(), false);

		String value = "30330220022002";
		byte[] bcdvalue = BCDASCII.fromASCIIToBCD(value.getBytes(), 0, value.length(), false);

		String asclen = new String().format("%02X", bcdvalue.length);
		byte[] bcdlength = BCDASCII.fromASCIIToBCD(asclen.getBytes(), 0, asclen.length(), false);

		byte[] field46 = ISO8583Util.byteArrayAdd(bcdtag, bcdlength, bcdvalue);
		return field46;
	}

	private String getMonthDay() {
		Time t = new Time();
		t.setToNow();
		String filled = "0";
		String month = String.valueOf(t.month + 1);
		String day = String.valueOf(t.monthDay);

		if (month.length() < 2) {
			month = filled + month;
		}
		if (day.length() < 2) {
			day = filled + day;
		}
		return month + day;
	}
}

