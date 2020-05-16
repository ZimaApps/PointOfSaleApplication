package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.secure.EncDec;
import com.standard.app.secure.SecureUtils;

import java.util.Arrays;

public class PackOnlineInit {
	private static final String TAG = Utils.TAGPUBLIC + PackOnlineInit.class.getSimpleName();

	public static final String PACK_ONLINE_INIT_TIME = "online_init";

	private ISO8583 mISO8583;
	private byte[]  mPacketMsg = null;

    public PackOnlineInit(String termId, String merId, String tradNum, int step) {
		Log.i(TAG, "PackOnlineInit, step = "+step);
        mISO8583 = new ISO8583();
        mISO8583.clearBit();

		String field11 = null;
		String field60 = null;
		byte[] field62 = new byte[16+8+20+20+20+20];
		String field63 = "99 ";
        
        mISO8583.setBit(0 , PackUtils.MSGTYPEID_ONLINE_INIT.getBytes(), PackUtils.MSGTYPEID_ONLINE_INIT.length());
		mISO8583.setBit(41, termId.getBytes(), termId.length());
		mISO8583.setBit(42, merId.getBytes(), merId.length());

		field11 = tradNum;
		mISO8583.setBit(11, field11.getBytes(), field11.length());

        try {
        	if (step == 1) {
				field60 = "54" + Utils.TEST_batchNum + "003";
				System.arraycopy(genField62Data(termId), 0, field62, 0, 16+8+20+20+20);
				mISO8583.setBit(62, field62, 16+8+20+20+20+20);
        	} else if (step == 2) {
				field60 = "52" + Utils.TEST_batchNum + "003";
        	} else if (step == 3) {
				field60 = "53" + Utils.TEST_batchNum + "003";
        	}
			mISO8583.setBit(60, field60.getBytes(), field60.length());
		} catch (Exception e) {
			e.printStackTrace();
		}

        mISO8583.setBit(63, field63.getBytes(), field63.length());
        
        byte[] isobyte = mISO8583.isotostr();
		mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
    }
    
    public byte[] get() {
    	return mPacketMsg;
    }
    
    private byte[] genField62Data(String termId) throws Exception {
    	byte[] xor1val= new byte[8];
    	byte[] xor2val= new byte[8];
    	byte[] ttekl  = new byte[16];
    	byte[] ttek   = new byte[24];
    	byte[] f62    = new byte[16+8+20+20+20+20];
    	String trd    = "22222222";  //for test
    	String pwd    = Utils.TEST_online_init_password;
    	
    	xor1val = BCDASCII.hexStringToBytes("CDA8C1AAD0C2D0CB");
        xor2val = BCDASCII.hexStringToBytes("D6A7B8B6CEDED3C7");

        System.arraycopy(SecureUtils.getTTEK(termId, pwd), 0, ttekl, 0, 16);
    	System.arraycopy(ttekl, 0, ttek, 0, 16);
    	System.arraycopy(ttekl, 0, ttek, 16, 8);
    	Log.i(TAG, "TTEK = " + BCDASCII.bytesToHexString(ttek));
    	
        Arrays.fill(f62, 0, 16+8+20+20+20+20, (byte)0x00);
    	System.arraycopy(EncDec.des3EncodeECB(ttek, trd.getBytes()), 0, f62, 0, 8);
    	System.arraycopy(EncDec.des3EncodeECB(ttek, pwd.getBytes()), 0, f62, 8, 8);
    	Log.i(TAG, "F62 = " + BCDASCII.bytesToHexString(f62));
    	return f62;
    }
}
