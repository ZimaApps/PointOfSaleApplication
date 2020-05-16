package com.lkl.cloudpos.aidl.emv;
import com.lkl.cloudpos.aidl.emv.AidlCheckCardListener;
import com.lkl.cloudpos.aidl.emv.AidlPbocStartListener;
import com.lkl.cloudpos.aidl.emv.EmvTransData;
interface AidlPboc{
	void checkCard(boolean supportMag,boolean supportIC ,boolean supportRF,int timeout,AidlCheckCardListener listener);
	void cancelCheckCard();
	void processPBOC(in EmvTransData transData, AidlPbocStartListener listener);
	void endPBOC();
	void abortPBOC();
	boolean  clearKernelICTransLog();
	int readKernelData(in String[] taglist,out byte[] buffer);
	void setTlv(String tag, in byte[] value);
	String parseTLV (String tag,String tlvlist);
	boolean importAmount(String amt);
	boolean importAidSelectRes(int index);
	boolean importPin(String pin);
	boolean importUserAuthRes(boolean res);
	boolean importMsgConfirmRes(boolean confirm);
	boolean importECashTipConfirmRes(boolean confirm);
	boolean importOnlineResp(boolean onlineRes,
	String respCode,String icc55);
	boolean updateAID(int optflag,String aid); 
	boolean updateCAPK(int optflag,String capk);
	boolean importConfirmCardInfoRes(boolean res);
	int isExistAidPublicKey();
}