package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.secure.EncryptControl;
import com.topwise.cloudpos.data.PinpadConstant;

public class UnpackSignup {
	private static final String TAG = Utils.TAGPUBLIC + UnpackSignup.class.getSimpleName();

	private String resMsg = null;
	private String resDetail = null;
	private EncryptControl mEncryptControl = new EncryptControl();

	public UnpackSignup(byte[] srcData, int srcDataLen) {
		Log.i(TAG, "UnPackSignup ... ");

		UnpackUtils unpack = new UnpackUtils();
		ISO8583 mIso = unpack.UnpackFront(srcData, srcDataLen);
		if (mIso == null)
			return;

		resMsg = new String(mIso.getBit(39));
		Log.i(TAG, "field 39 is：" + resMsg);
		resDetail = unpack.processField46(mIso.getBit(46), resMsg);

		if ("00".equals(resMsg)) {
			int result = 0;

			//解析60域，获取批次号
			if (mIso.getBit(60) == null || mIso.getBit(60).length == 0) {
				resDetail = "批次号获取失败";
				return;
			} else {
				String f60 = new String(mIso.getBit(60));
//				app.mTerm.setBatchSn(f60.substring(2, 8));
				Log.i(TAG, f60.substring(2, 8));
			}
			//解析62域
			try {
				Log.i(TAG, "解析出tpk /tak /tmk /trk...");

				byte[] tmptrk = new byte[24];
				byte[] tmptrkdes = new byte[24];

				byte[] tmpval = new byte[16];
				byte[] vtpk = new byte[16];
				byte[] vtak = new byte[16];
				byte[] tmk = new byte[24];
				byte[] tpk = new byte[24];
				byte[] tak = new byte[24];
				byte[] trk = new byte[24];
				byte[] checkv = new byte[4];
				byte[] check = new byte[4];

				//TPK
				System.arraycopy(mIso.getBit(62), 24 + 20, tmpval, 0, 16);
				System.arraycopy(mIso.getBit(62), 24 + 20 + 16, checkv, 0, 4);

				Log.i(TAG, "UpdateWorkKey ------1");
				Log.i(TAG, "TPK = "+BCDASCII.bytesToHexString(tmpval));
				result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_PIK, BCDASCII.bytesToHexString(tmpval, 16), null/*BCDASCII.bytesToHexString(checkv)*/);
				if (result != 0) {
					resDetail = "01pin密钥下载失败";
					return;
				}

				//TAK
				System.arraycopy(mIso.getBit(62), 24 + 20 + 20, tmpval, 0, 8);
				System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 16, checkv, 0, 4);

				Log.i(TAG, "UpdateWorkKey ------2");
				Log.i(TAG, "TAK = "+BCDASCII.bytesToHexString(tmpval));
				result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_MAK, BCDASCII.bytesToHexString(tmpval, 8), null/*BCDASCII.bytesToHexString(checkv)*/);
				if (result != 0) {
					resDetail = "01mac密钥下载失败";
					return;
				}

				//TRK
				System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 20, tmpval, 0, 16);
				System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 20 + 16, checkv, 0, 4);

				Log.i(TAG, "UpdateWorkKey ------3");
				Log.i(TAG, "TRK = "+BCDASCII.bytesToHexString(tmpval));
				result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_TDK, BCDASCII.bytesToHexString(tmpval, 16), null/*BCDASCII.bytesToHexString(checkv)*/);
				Log.i(TAG, "result " + result);
				if (result != 0) {
					resDetail = "01TRK密钥下载失败";
					return;
				}
			} catch (Exception e) {
				Log.i(TAG, "error " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public String getResponse() {
		return resMsg;
	}

	public String getResponseDetail() {
		return resDetail;
	}
}
