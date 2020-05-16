package com.lkl.cloudpos.aidl.shellmonitor;

//ShellMonitor
interface AidlShellMonitor{
	
	boolean executeCmd(String cmd);//root用户下执行shell命令
	void recovery();//恢复出厂设置
	boolean canRecovery(); //当前终端是否能够恢复出厂设置

	byte[] getRootAuth(String  rootAuth);
 boolean executeRootCMD(String  rootkey,  String authToken, String cmdParams);
    String getHardwareSNPlaintext();
    byte[] getHardwareSNCiphertext(in byte[] b);
}
