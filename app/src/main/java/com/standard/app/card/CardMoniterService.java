package com.standard.app.card;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.standard.app.DeviceTopUsdkServiceManager;
import com.standard.app.Utils;
import com.topwise.cloudpos.aidl.emv.AidlPboc;

public class CardMoniterService extends Service {
    private final String TAG = Utils.TAGPUBLIC + CardMoniterService.class.getSimpleName();
    private static final int SEARCH_CARD_TIME = 30000;

    private AidlPboc mPbocManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");

        mPbocManager = DeviceTopUsdkServiceManager.getInstance().getPbocManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        try {
            if(mPbocManager != null){
                mPbocManager.endPBOC();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        checkCard(true, true, true);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    private void checkCard(boolean isMag, boolean isIc, boolean isRf) {
        Log.i(TAG, "searchCard()");
        synchronized (this) {
            try {
                if(mPbocManager != null){
                    mPbocManager.checkCard(isMag, isIc, isRf, SEARCH_CARD_TIME, new CheckCardListenerSub(this));
                    Log.i("CARD CHECK", " ISMAGNETIC "+isMag+" ISIC " +isIc+" ISRF "+ isRf +" SEARCHTIME "+ SEARCH_CARD_TIME+" CARD LISTERNER "+ new CheckCardListenerSub(this));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelCheckCard() {
        Log.i(TAG, "cancelCheckCard()");
        synchronized (this) {
            try {
                if(mPbocManager != null){
                    mPbocManager.cancelCheckCard();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        cancelCheckCard();
    }
}

