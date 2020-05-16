package com.standard.app.packet;

import android.util.Log;

import com.standard.app.Utils;
import com.standard.app.iso8583.BCDASCII;
import com.standard.app.iso8583.ISO8583;
import com.standard.app.secure.EncDec;
import com.standard.app.secure.EncryptControl;
import com.standard.app.secure.Md5;
import com.standard.app.secure.SecureUtils;

import java.security.NoSuchAlgorithmException;

public class UnpackOnlineInit {
	private static final String TAG = Utils.TAGPUBLIC + UnpackOnlineInit.class.getSimpleName();

	private String resMsg = null;
	private String resMsgDetail = null;
	private byte[] mTmk;
	private EncryptControl mEncryptControl = new EncryptControl();

	int result;

	public void onlineInit(String termId, int step, byte[] srcData, int srcDataLen) {
		Log.d(TAG, "onlineInit ... , step = "+step);

		UnpackUtils unpack = new UnpackUtils();
		ISO8583 mIso = unpack.UnpackFront(srcData, srcDataLen);
		if(mIso == null)
			return;

		resMsg = new String(mIso.getBit(39));
		Log.d(TAG,"field 39 is：" + resMsg);
//		resMsgDetail = UnPacketUtils.processField46(app, mIso.getBit(46), resMsg);

		if(resMsg.equals("00")){

			if (step == 3) return;

			if (step == 1) {
				//F62
				String pwd    = Utils.TEST_online_init_password;
				String trd    = "22222222";  	//for test
				String prd    = "";				//new String(); //"11111111";
				byte[] bprd   = new byte[8];	//密文PRD, mIso.getBit(62);
				byte[] pprd   = new byte[8];	//明文PRD
				byte[] xor1val= new byte[8];
				byte[] xor2val= new byte[8];
				byte[] xtrd   = new byte[8];
				byte[] xprd   = new byte[8];
				byte[] ttekkey= new byte[24];
				byte[] itekkey= new byte[24];
				byte[] ttek   = new byte[16];
				byte[] itek   = new byte[16];
				byte[] itek_left = new byte[8];
				byte[] itek_right= new byte[8];
				byte[] md5pwd    = new byte[16];
				byte[] itmk      = new byte[24];
				byte[] vitmk = new byte[16];
				byte[] checkv= new byte[4];
				byte[] check = new byte[4];

				System.arraycopy(SecureUtils.GetXOR1Val(), 0, xor1val, 0, 8);
				System.arraycopy(SecureUtils.getXTID(termId , xor1val), 0, itek_left, 0, 8);

				System.arraycopy(SecureUtils.getTTEK(termId, pwd), 0, ttek, 0, 16);
				System.arraycopy(ttek, 0, ttekkey, 0, 16);
				System.arraycopy(ttek, 0, ttekkey, 16, 8);
				Log.d(TAG, "TTEK is : "+ BCDASCII.bytesToHexString(ttek));
				try {
					//PRD明文
					System.arraycopy(mIso.getBit(62), 16, bprd, 0, 8);
					Log.d(TAG, "密文PRD is : "+ BCDASCII.bytesToHexString(bprd));
					System.arraycopy(EncDec.des3DecodeECB(ttekkey, bprd), 0, pprd, 0, 8);
					Log.d(TAG, "Plain PRD is : "+ BCDASCII.bytesToHexString(pprd));
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.arraycopy(SecureUtils.getXTID(trd , xor1val), 0, xtrd, 0, 8);
				System.arraycopy(SecureUtils.getXTID(pprd, xor1val), 0, xprd, 0, 8);
				System.arraycopy(SecureUtils.XOR(xtrd, xprd, 8), 0, itek_right, 0, 8);
				Log.d(TAG, "XTrd is :"+ BCDASCII.bytesToHexString(xtrd));
				Log.d(TAG, "XPrd is :"+ BCDASCII.bytesToHexString(xprd));
				Log.d(TAG, "itek_left  is :"+ BCDASCII.bytesToHexString(itek_left));
				Log.d(TAG, "itek_right is :"+ BCDASCII.bytesToHexString(itek_right));

				System.arraycopy(itek_left , 0, itek, 0, 8);
				System.arraycopy(itek_right, 0, itek, 8, 8);
				try {
					String md5result = Md5.gen(pwd);
					md5pwd = BCDASCII.hexStringToBytes(md5result);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				System.arraycopy(SecureUtils.XOR(md5pwd, itek, 16), 0, itek, 0, 16);
				Log.d(TAG, "itek is :"+ BCDASCII.bytesToHexString(itek));
				byte[] field_62 = mIso.getBit(62);
				if(field_62 != null || field_62.length != 0){
					System.arraycopy(mIso.getBit(62), 16+8,    vitmk,  0, 16);
					System.arraycopy(mIso.getBit(62), 16+8+16, checkv, 0, 4);
				}

				try {
					System.arraycopy(itek, 0, itekkey, 0, 16);
					System.arraycopy(itek, 0, itekkey, 16, 8);
					System.arraycopy(EncDec.des3DecodeECB(itekkey, vitmk), 0, itmk, 0, 16);
					Log.d(TAG, "ITMK = "+BCDASCII.bytesToHexString(itmk));
					System.arraycopy(itmk, 0, itmk, 16, 8);
					System.arraycopy(EncDec.des3EncodeECB(itmk, BCDASCII.hexStringToBytes("0000000000000000")), 0, check, 0, 4);
					if (new String(check).equals(new String(checkv))) {
						mTmk = itmk;
					} else {
						Log.d(TAG, "不相等CHECKVALUE\n报文checkvalue="+BCDASCII.bytesToHexString(checkv)+",计算后="+BCDASCII.bytesToHexString(check));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (step == 2) {
				Log.d(TAG, "解析tpk /tak /tmk /trk...");
				byte[] tmpval = new byte[16];
				byte[] vtpk = new byte[16];
				byte[] vtak = new byte[16];
				byte[] tmk = new byte[24];
				byte[] tpk = new byte[24];
				byte[] tak = new byte[24];
				byte[] trk = new byte[24];
				byte[] checkv = new byte[4];
				byte[] check = new byte[4];

				try {
					//TMK
					System.arraycopy(mIso.getBit(62), 24, tmpval, 0, 16);
					System.arraycopy(mIso.getBit(62), 24 + 16, checkv, 0, 4);
					System.arraycopy(EncDec.des3DecodeECB(mTmk, tmpval), 0, tmk, 0, 16);
					Log.d(TAG, "ITMK   = " + BCDASCII.bytesToHexString(mTmk));
					Log.d(TAG, "TMK 密文= " + BCDASCII.bytesToHexString(tmpval));
					Log.d(TAG, "TMK 明文= " + BCDASCII.bytesToHexString(tmk));

					System.arraycopy(tmk, 0, tmk, 16, 8);
					System.arraycopy(EncDec.des3EncodeECB(tmk, BCDASCII.hexStringToBytes("0000000000000000")), 0, check, 0, 4);
					if (new String(check).equals(new String(checkv))) {
						Log.d(TAG, "相等CHECKVALUE");
						result = mEncryptControl.UpdateMasterKey(BCDASCII.bytesToHexString(tmk));
						Log.d(TAG, "UpdateMasterKey result = "+result+", tmk = "+BCDASCII.bytesToHexString(tmk));

						if (result != 0){
							resMsgDetail = "01主密钥下载失败";
							return;
						}
					} else {
						Log.d(TAG, "不相等CHECKVALUE\n报文 checkvalue=" + BCDASCII.bytesToHexString(checkv) + ",计算后=" + BCDASCII.bytesToHexString(check));
					}

					//TPK
					System.arraycopy(mIso.getBit(62), 24 + 20, tmpval, 0, 16);
					System.arraycopy(mIso.getBit(62), 24 + 20 + 16, checkv, 0, 4);
					System.arraycopy(EncDec.des3DecodeECB(tmk, tmpval), 0, tpk, 0, 16);
					/*result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_PIK, BCDASCII.bytesToHexString(tmpval, 16), BCDASCII.bytesToHexString(checkv));
					if (result != 0){
						resMsgDetail = "01TPK下载失败";
						return;
					}*/
					Log.d(TAG, "TPK密文 = " + BCDASCII.bytesToHexString(tmpval));
					Log.d(TAG, "TPK明文 = " + BCDASCII.bytesToHexString(tpk));

					System.arraycopy(tpk, 0, tpk, 16, 8);
					System.arraycopy(EncDec.des3EncodeECB(tpk, BCDASCII.hexStringToBytes("0000000000000000")), 0, check, 0, 4);

					//TAK
					System.arraycopy(mIso.getBit(62), 24 + 20 + 20, tmpval, 0, 8);
					System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 16, checkv, 0, 4);
					System.arraycopy(EncDec.des3DecodeECB(tmk, tmpval), 0, tak, 0, 8);
					/*result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_MAK, BCDASCII.bytesToHexString(tmpval, 8), BCDASCII.bytesToHexString(checkv));
					if (result != 0){
						resMsgDetail = "01TAK下载失败";
						return;
					}*/
					Log.d(TAG, "TAK密文 = " + BCDASCII.bytesToHexString(tmpval));
					Log.d(TAG, "TAK明文 = " + BCDASCII.bytesToHexString(tak));

					System.arraycopy(EncDec.des3EncodeECB(tak, BCDASCII.hexStringToBytes("0000000000000000")), 0, check, 0, 4);

					//TRK
					System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 20, tmpval, 0, 16);
					System.arraycopy(mIso.getBit(62), 24 + 20 + 20 + 20 + 16, checkv, 0, 4);
					System.arraycopy(EncDec.des3DecodeECB(tmk, tmpval), 0, trk, 0, 16);
					/*result = mEncryptControl.UpdateWorkKey(PinpadConstant.WKeyType.WKEY_TYPE_TDK, BCDASCII.bytesToHexString(tmpval, 16), BCDASCII.bytesToHexString(checkv));
					if (result != 0){
						resMsgDetail = "01TRK下载失败";
						return;
					}*/
					Log.d(TAG, "TRK密文 = " + BCDASCII.bytesToHexString(tmpval));
					Log.d(TAG, "TRK明文 = " + BCDASCII.bytesToHexString(trk));

					System.arraycopy(trk, 0, trk, 16, 8);
					System.arraycopy(EncDec.des3EncodeECB(trk, BCDASCII.hexStringToBytes("0000000000000000")), 0, check, 0, 4);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getResponse() {
		return resMsg;
	}

	public String getResponseDetail() {
		return resMsgDetail;
	}
}
