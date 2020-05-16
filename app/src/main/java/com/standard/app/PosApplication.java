package com.standard.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.standard.app.cache.ConsumeData;

public class PosApplication extends Application{
	private static final String TAG = Utils.TAGPUBLIC + PosApplication.class.getSimpleName();

	private Context mContext;
	private static PosApplication mPosApplication;

	public ConsumeData mConsumeData;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		mContext = getApplicationContext();
		mPosApplication = this;
	}

	public static PosApplication getApp() {
		return mPosApplication;
	}

	public void getDeviceManager() {
		DeviceTopUsdkServiceManager.getInstance();
	}

	public void setConsumeData() {
		mConsumeData = new ConsumeData();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminate");
		System.exit(0);
	}

}
