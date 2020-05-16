package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;

import java.io.UnsupportedEncodingException;

public class UnpackScan {
	private static final String TAG = Utils.TAGPUBLIC + UnpackScan.class.getSimpleName();

	private String resPonse = null;
	private String resDetail = null;
	private byte[] mField47;
	private String mPrintMsg = null;

	public UnpackScan(byte[] srcData, int srcDataLen) {
		Log.d(TAG, "UnpackScan ... ");

		UnpackUtils unpack = new UnpackUtils();
		ISO8583 mIso = unpack.UnpackFront(srcData, srcDataLen);
		if(mIso == null)
			return;

		resPonse = new String(mIso.getBit(39));
		Log.d(TAG,"field 39 is：" + resPonse);
		resDetail = unpack.processField46(mIso.getBit(46), resPonse);

		if (resPonse.equals("00")) {
			Log.d(TAG, "###########4###################");
			byte[] field4 = mIso.getBit(4);
			Log.d(TAG, "4 " + field4.length);
			Log.d(TAG, "4 ASCII: " + new String(field4));
			Log.d(TAG,"4 BCD: "+ BCDASCII.bytesToHexString(BCDASCII.fromASCIIToBCD(field4, 0, field4.length,false),
					BCDASCII.fromASCIIToBCD(field4, 0, field4.length, false).length));

			Log.d(TAG, "###########11###################");
			byte[] field11 = mIso.getBit(11);
			Log.d(TAG, "11:" + field11.length);
			Log.d(TAG, "11  ASCII: " + new String(field11));
			Log.d(TAG,"11  BCD: " + BCDASCII.bytesToHexString(BCDASCII.fromASCIIToBCD(field11, 0, field11.length,false),
					BCDASCII.fromASCIIToBCD(field11, 0, field11.length, false).length));
			if(field11 != null && field11.length != 0) {
			}
			Log.d(TAG, "###########12####################");
			byte[] field12 = mIso.getBit(12);
			Log.d(TAG, "12:" + field12.length);
			Log.d(TAG, "12 ASCII: " + new String(field12));

			Log.d(TAG, "##########13##################");
			byte[] field13 = mIso.getBit(13);
			Log.d(TAG, "13:" + field13.length);
			Log.d(TAG, "13 ASCII: " + new String(field13));

			Log.d(TAG, "###########41####################");
			byte[] field41 = mIso.getBit(41);
			Log.d(TAG, "41:" + field41.length);
			Log.d(TAG, "41  ASCII: " + new String(field41));

			Log.d(TAG, "###########42####################");
			byte[] field42 = mIso.getBit(42);
			Log.d(TAG, "42:" + field42.length);
			Log.d(TAG, "42  ASCII: " + new String(field42));

			Log.d(TAG, "###########47####################");
			mField47 = mIso.getBit(47);
			Log.i(TAG,"mField47: "+BCDASCII.bytesToHexString(mField47));

			byte[] printByte = new byte[mField47.length - 5];
			int printLen = printByte.length;
			System.arraycopy(mField47, 5, printByte, 0, printLen);
			Log.i(TAG,"printByte: "+BCDASCII.bytesToHexString(printByte));
			for (int i = 0; i < printLen; i++) {
				if (printByte[i] == 0x10 || printByte[i] == 0x11
						|| printByte[i] == 0x12 || printByte[i] == 0x13) {
					printByte[i] = 0x0A;
				}
				if (printByte[i] == 0x02) {
					printByte[i] = 0x00;
				}
				if (printByte[i] == 0xA1) {

				}
			}
			Log.d(TAG, "printByte:" + printByte.length);
			try {
				mPrintMsg = new String(printByte, "GBK");
				Log.d(TAG, "47 ASCII: " + mPrintMsg);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getResponse() {
		return resPonse;
	}

	public String getResponseDetail() {
		return resDetail;
	}

	public byte[] getField47() {
		return mField47;
	}
}
