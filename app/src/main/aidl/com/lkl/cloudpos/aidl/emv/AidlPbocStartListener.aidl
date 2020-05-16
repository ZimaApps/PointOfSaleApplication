package com.lkl.cloudpos.aidl.emv;
import com.lkl.cloudpos.aidl.emv.PCardLoadLog;
import com.lkl.cloudpos.aidl.emv.PCardTransLog;
import com.lkl.cloudpos.aidl.emv.CardInfo;
//PBOC监听器
interface AidlPbocStartListener{
	/**请求输入金额 ，简易流程时不回调此方法*/
	void requestImportAmount(int type);	
	/**请求提示信息*/
	void requestTipsConfirm(String msg);	
	/**请求多应用选择*/
	void requestAidSelect(int times,in String[] aids);	/**请求确认是否使用电子现金*/
	void requestEcashTipsConfirm();	
	/**请求确认卡信息*/
	void onConfirmCardInfo(in CardInfo cardInfo);	
	/** 请求导入PIN */
	void requestImportPin(int type,boolean lasttimeFlag,String amt);
	/** 请求身份认证 */
	void requestUserAuth(int certype,String certnumber);
	/**请求联机*/
	void onRequestOnline();	
	/**返回读取卡片脱机余额结果*/
	void onReadCardOffLineBalance(String moneyCode,String balance,String secondMoneyCode,String secondBalance);	
	/**返回读取卡片交易日志结果*/
	void onReadCardTransLog(in PCardTransLog[] log);	
	/**返回读取卡片圈存日志结果*/
	void onReadCardLoadLog(String atc,String checkCode,in PCardLoadLog[] logs);
	/**交易结果
	批准: 0x01
	拒绝: 0x02
	终止: 0x03
	FALLBACK: 0x04
	采用其他界面: 0x05
	其他：0x06
	EMV简易流程不回调此方法
	*/
	void onTransResult(int result);
	/**出错*/
	void onError(int erroCode);	
}