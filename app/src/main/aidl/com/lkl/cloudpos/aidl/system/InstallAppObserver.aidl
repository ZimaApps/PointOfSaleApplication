package com.lkl.cloudpos.aidl.system;
//APP安装监听器
interface InstallAppObserver{
	//安装成功
	void onInstallFinished();
	//安装错误
	void onInstallError(int errorCode);
}