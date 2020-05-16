package com.lkl.cloudpos.aidl;
import com.lkl.cloudpos.aidl.camera.AidlCameraScanCodeListener;
interface InnerScanner{
	void initScanner(in Bundle bundle);
	
	int startScan(int timeout, in AidlCameraScanCodeListener listener);
	
	void stopScan();
}