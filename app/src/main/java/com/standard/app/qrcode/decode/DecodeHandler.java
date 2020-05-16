/*
 * Copyright (C) 2010 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.standard.app.qrcode.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.qrcode.QRCodeReader;
import com.standard.app.R;
import com.standard.app.qrcode.QrCodeActivity;
import com.standard.app.qrcode.utils.SDKLog;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.Hashtable;
import java.util.Map;

//import com.android.topwise.sdk.utils.SDKLog;

/**
 * zbar imports
 */
final class DecodeHandler extends Handler {

    private final QrCodeActivity mActivity;
    private final QRCodeReader mQrCodeReader;
    private final Map<DecodeHintType, Object> mHints;
    private byte[] mRotatedData;
    private long timePriv;
    private ImageScanner scanner;

    DecodeHandler(QrCodeActivity activity) {
        this.mActivity = activity;
        mQrCodeReader = new QRCodeReader();
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        scanner = new ImageScanner();
        /* Instance barcode scanner */
        scanner.setConfig(0, Config.X_DENSITY, 1);
        scanner.setConfig(0, Config.Y_DENSITY, 2);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                Looper looper = Looper.myLooper();
                if (null != looper) {
                    looper.quit();
                }
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.
     *
     * @param data The YUV preview frame.
     * @param width The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        SDKLog.d("qrd","scanning  start (width,height) = "+"("+width+","+ height+")");
        timePriv = System.currentTimeMillis();
        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);

        int result = scanner.scanImage(barcode);
        SDKLog.d("qrd","scanning end ,time consumed ="+(System.currentTimeMillis()-timePriv));

        if (result != 0) {
            SDKLog.d("qrd","Scan result is OK , all time consumed ="+(System.currentTimeMillis()-timePriv));
            SymbolSet syms = scanner.getResults();
            StringBuilder sb = new StringBuilder();
            for (Symbol sym : syms) {
                sb.append(sym.getData());
            }
            Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_succeeded, sb.toString());
            message.sendToTarget();
        } else {
            Message message = Message.obtain(mActivity.getCaptureActivityHandler(), R.id.decode_failed);
            message.sendToTarget();
        }
    }
}
