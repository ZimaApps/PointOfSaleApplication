package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.FrontHeader;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.iso8583.ISO8583Util;
import com.standard.app.iso8583.MessageHeader;
import com.standard.app.iso8583.TPDU;
import com.standard.app.secure.EncryptControl;

import java.util.Arrays;

import static com.standard.app.iso8583.ISO8583.LLLVAR_LEN;
import static com.standard.app.iso8583.ISO8583.LLVAR_LEN;

public class PackUtils {
    private static final String TAG = Utils.TAGPUBLIC + PackUtils.class.getSimpleName();

    public static final String MSGTYPEID_LOG_IN = "0800";
    public static final String MSGTYPEID_LOG_OFF = "0800";
    public static final String MSGTYPEID_ONLINE_INIT = "0800";
    public static final String MSGTYPEID_PARA_TRANS = "0800";
    public static final String MSGTYPEID_PARA_DOWNLOAD = "0800";
    public static final String MSGTYPEID_STATUS_UPLOAD = "0820";
    public static final String MSGTYPEID_ECHOTEST = "0820";

    public static final String MSGTYPEID_EMV_QUERY = "0820";
    public static final String MSGTYPEID_EMV_DOWNLOAD = "0800";
    public static final String MSGTYPEID_EMV_DOWNLOAD_END = "0800";

    public static final String MSGTYPEID_CONSUME = "0200";
    public static final String MSGTYPEID_POSTIVE = "0400";

    public static final String MSGTYPEID_SCAN = "0200";

    protected static byte[] getPacketHeader(byte[] isobyte, String termId, String merId) {
        //TPDU
        Log.d(TAG, "TPDU=" + BCDASCII.bytesToHexString(TPDU.get()));
        //Header
        MessageHeader.setAppType((byte) 0x80);
        MessageHeader.setTermStat((byte) 0x00);
        MessageHeader.setProcCode((byte) 0x00);
        Log.d(TAG, "MessageHeader=" + BCDASCII.bytesToHexString(MessageHeader.get()));
        //Front
        FrontHeader.setLen(isobyte.length);
        Log.d(TAG, "FrontHeader=" + BCDASCII.bytesToHexString(FrontHeader.get(termId, merId)) + " , ascii(" + new String(FrontHeader.get(termId, merId)) + ")");

        String cs = String.format("%04X", ISO8583Util.byteArrayAdd(isobyte, ISO8583Util.byteArrayAdd(TPDU.get(), MessageHeader.get(), FrontHeader.get(termId, merId))).length);
        byte[] bcdlen = BCDASCII.hexStringToBytes(cs);

        return ISO8583Util.byteArrayAdd(bcdlen, ISO8583Util.byteArrayAdd(TPDU.get(), MessageHeader.get(), FrontHeader.get(termId, merId)), isobyte);
    }

    protected static String getField4(String amountStr) {
        Log.i(TAG, "begin amount: " + amountStr);
        int index = amountStr.indexOf(".");
        if (amountStr.substring(index + 1, amountStr.length()).length() < 2) {
            amountStr = amountStr + "0";
        }
        amountStr = amountStr.replace(".", "");
        int amtlen = amountStr.length();
        StringBuilder amtBuilder = new StringBuilder();
        if (amtlen < 12) {
            for (int i = 0; i < (12 - amtlen); i++) {
                amtBuilder.append("0");
            }
        }
        amtBuilder.append(amountStr);
        amountStr = amtBuilder.toString();
        Log.i(TAG, "begin amount: " + amountStr);
        return amountStr;
    }

    protected static byte[] getTrackField(String data, int lenType) {
        Log.i(TAG, "getTrackField(), data = " + data + ", lenType = " + lenType);
        byte[] lenData = null;
        byte[] byteData = null;

        byte[] inputData = null;
        byte[] outputDate = null;

        int lenReal;

        if (lenType == LLVAR_LEN) {
            lenData = BCDASCII.hexStringToBytes(String.format("%2d", data.length()));
        } else if (lenType == LLLVAR_LEN) {
            lenData = BCDASCII.hexStringToBytes(String.format("%4d", data.length()));
        }
        Log.i(TAG, "lenData = "+BCDASCII.bytesToHexString(lenData));

        if (data.length() % 2 != 0) {
            byteData = BCDASCII.hexStringToBytes(data+"0");
        } else {
            byteData = BCDASCII.hexStringToBytes(data);
        }
        Log.i(TAG, "byteData = "+BCDASCII.bytesToHexString(byteData));

        lenReal = (lenData.length + byteData.length) + 8 - (lenData.length + byteData.length) % 8 ;
        inputData = new byte[lenReal];
        Arrays.fill(inputData, (byte) 0x00);
        System.arraycopy(lenData, 0, inputData, 0, lenData.length);
        System.arraycopy(byteData, 0, inputData, lenData.length, byteData.length);
        Log.i(TAG, "inputData = "+BCDASCII.bytesToHexString(inputData));

        outputDate = new byte[lenReal];
        try {
            EncryptControl encryptControl = new EncryptControl();
            int result = encryptControl.CalculateEncryptByTDK(inputData, outputDate);
            if (result != 0) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "outputDate = "+BCDASCII.bytesToHexString(outputDate));

        return outputDate;
    }

	protected static byte[] getField64(ISO8583 iso8583) {
		byte[] nulldata = new byte[8];
        Arrays.fill(nulldata, (byte) 0x00);
		iso8583.setBit(64, nulldata, nulldata.length);
		byte[] mac = new byte[8];
        EncryptControl encryptControl = new EncryptControl();
        encryptControl.CalculateMac(iso8583.isotostr(), iso8583.isotostr().length, mac);
        return mac;
	}

    public static String getfixedNumber(int number , int len, String fixChar) {
        String fixedNum = "";
        String num = Integer.toString(number);
        int numLen = num.length();
        while (numLen < len) {
            fixedNum += fixChar;
            numLen++;
        }
        fixedNum = fixedNum + number;
        return fixedNum;
    }
}
