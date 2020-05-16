package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;

public class PackStatusUpload {
	private static final String  TAG = Utils.TAGPUBLIC + PackStatusUpload.class.getSimpleName();

	private ISO8583 mISO8583;
	private byte[] mPacketMsg = null;
	
	public PackStatusUpload(String termId, String merId, String tpdu) {
        Log.i(TAG, "PackStatusUpload()");

        mISO8583 = new ISO8583();
        mISO8583.clearBit();

        byte[] field46 = null;
        String field60 = null;
        String field62 = null;
        
        mISO8583.setBit(0 , PackUtils.MSGTYPEID_STATUS_UPLOAD.getBytes(), PackUtils.MSGTYPEID_STATUS_UPLOAD.length());
        mISO8583.setBit(41, termId.getBytes(), termId.length());
        mISO8583.setBit(42, merId.getBytes(), merId.length());

        field46 = getField46(tpdu);
        mISO8583.setBit(46, field46, field46.length);

        field60 = "02" + Utils.TEST_batchNum + "362";
        mISO8583.setBit(60, field60.getBytes(),field60.length());

        field62 = getField62();
        mISO8583.setBit(62, field62.getBytes(), field62.length());
        
        byte[] isobyte = mISO8583.isotostr();
        mPacketMsg = PackUtils.getPacketHeader(isobyte, termId, merId);
	}

	public byte[] get()
	{
		return mPacketMsg;
	}

    private byte[] getField46(String tpdu) {
        String T6F08 = "99990000" + tpdu + "5569" + Utils.TEST_progRelDate + Utils.TEST_progVer + Utils.TEST_vendorCode + Utils.TEST_merType + Utils.TEST_termSeqNo;
        byte[] tmpb = new byte[T6F08.length() + 3];
        tmpb[0] = 0x6F;
        tmpb[1] = 0x08;
        tmpb[2] = (byte) T6F08.length();
        System.arraycopy(T6F08.getBytes(), 0, tmpb, 3, T6F08.length());
        Log.i(TAG, "T6F08 = " + T6F08);
        Log.i(TAG, "tmpb = " + BCDASCII.bytesToHexString(tmpb));
        return tmpb;
    }

    private String getField62() {
        String f62 = "011021031041051" + "1160" +"12";

        String timeout = "00" + Utils.TEST_default_trans_timeout;
        f62 = f62 + timeout.substring(timeout.length()-2) + "13";
        Log.i(TAG, "timeout = "+timeout);
        Log.i(TAG, "f62_1 = "+f62);

        String redial = "0" + Utils.TEST_default_trans_redial_tims;
        f62 = f62 + redial.substring(redial.length()-1) + "14";
        Log.i(TAG, "redial = "+redial);
        Log.i(TAG, "f62_2 = "+f62);

        String dialNo = "              " + Utils.TEST_dial_host_number1;
        f62 = f62 + dialNo.substring(dialNo.length()-14) + "15";
        Log.i(TAG, "dialNo = "+dialNo);
        Log.i(TAG, "f62_3 = "+f62);

        dialNo = "              " + Utils.TEST_dial_host_number2;
        f62 = f62 + dialNo.substring(dialNo.length()-14) + "16";
        Log.i(TAG, "dialNo = "+dialNo);
        Log.i(TAG, "f62_4 = "+f62);

        dialNo = "              " + Utils.TEST_dial_host_number3;
//        dialNo = "              " + mApp.mComm.getDialHost3();
        f62 = f62 + dialNo.substring(dialNo.length()-14) + "17";
        Log.i(TAG, "dialNo = "+dialNo);
        Log.i(TAG, "f62_5 = "+f62);

        dialNo = "              " + Utils.TEST_dial_host_number4;
//        dialNo = "              " + mApp.mComm.getDialCenter();
        f62 = f62 + dialNo.substring(dialNo.length()-14) + "18";
        Log.i(TAG, "dialNo = "+dialNo);
        Log.i(TAG, "f62_6 = "+f62);

        if(Utils.TEST_support_tip)
            f62 = f62 + "119";
        else
            f62 = f62 + "019";
        Log.i(TAG, "Utils.TEST_support_tip = "+Utils.TEST_support_tip);
        Log.i(TAG, "f62_7 = "+f62);

        String tipPer = "00" + Utils.TEST_support_tip_radio;
        f62 = f62 + tipPer.substring(tipPer.length()-2) + "20";
        Log.i(TAG, "tipPer = "+tipPer);
        Log.i(TAG, "f62_8 = "+f62);

        if(Utils.TEST_support_all_input_card_num)
            f62 = f62 +"121";
        else
            f62 = f62 +"021";
        Log.i(TAG, "Utils.TEST_support_all_input_card_num = "+Utils.TEST_support_all_input_card_num);
        Log.i(TAG, "f62_9 = "+f62);

        if(Utils.TEST_support_auto_sign_off_after_settle)
            f62 = f62 +"123"; //122  商户号
        else
            f62 = f62 +"023"; //022商户号
        Log.i(TAG, "Utils.TEST_support_auto_sign_off_after_settle = "+Utils.TEST_support_auto_sign_off_after_settle);
        Log.i(TAG, "f62_10 = "+f62);

        String retry = "0" + Utils.TEST_support_resend_packet_times;
        f62 = f62 + retry.substring(retry.length()-1) + "24";
        Log.i(TAG, "retry = "+retry);
        Log.i(TAG, "f62_11 = "+f62);

        //离线交易上送方式
        f62 = f62 + "1";

        f62 = f62 + "258";

        f62 = f62 + "51001100111098";
        Log.i(TAG, "f62_12 = "+f62);
        return f62;
    }
}
