package com.standard.app.paycorpHttp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.standard.app.Utils;
import com.standard.app.activity.SendPacketResultActivity;
import com.standard.app.card.CardManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JsonAndHttpsUtils {

    private static final String TAG = Utils.TAGPUBLIC + JsonAndHttpsUtils.class.getSimpleName();
    public static final String AUTHTOKEN = "e778eb18-79e2-428b-8188-c37973177537";
    public static final String HMAC_SECRET = "23YrQRohTw2F8Plt";
    public static final String REQUEST_URL = "https://combank.paycorp.com.au/rest/service/proxy/";

//    private static class LazyHolder {
//        private static final JsonAndHttpsUtils INSTANCE = new JsonAndHttpsUtils();
//    }
//
//    private JsonAndHttpsUtils ( ){
//
//    }
//
//    public static final JsonAndHttpsUtils getInstance( ) {
//        return LazyHolder.INSTANCE;
//    }

    private static String getJsonMessage(String tlvData){
        Log.d(TAG, "getJsonMessage() called with: tlvData = [" + tlvData + "]");

        String msgId = UUID.randomUUID().toString();

        RequestData requestData = new RequestData(false,
                "PAYMENT_MPOS",
                getSimpleTimestamp(),
                tlvData);

        JsonRootBean jsonRootBean = new JsonRootBean("1.0.4",
                msgId,
                getISO8601Timestamp(),
                false,
                "mpos",
                "PAYMENT",
                "MPOS",
                requestData);

        Gson gson = new Gson();
        String json = gson.toJson(jsonRootBean);

        Log.d(TAG, "getJsonMessage() called return: json = [" + json + "]");
        return json;
    }

    /**
     * 传入Data类型日期，返回字符串类型时间（ISO8601标准时间）
     * @param
     * @return
     */
    private static String getISO8601Timestamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+0530'");
        String nowAsISO = df.format(System.currentTimeMillis());
        Log.d(TAG, "new Time: " + nowAsISO);
        return nowAsISO;
    }

    /**
     * 传入Data类型日期，返回字符串类型时间 (常用简单格式)
     * @return
     */
    private static String getSimpleTimestamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowAsSIM = df.format(System.currentTimeMillis());
        return nowAsSIM;
    }

    /**
     *
     * @param tlvData
     * @return
     */
    public static void sendJsonData(final Context context, String tlvData) {
        Log.d(TAG, "sendJsonData() called with: tlvData = [" + tlvData + "]");
        final Bundle bundle = new Bundle();
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        String json = getJsonMessage(tlvData);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        Request request = new Request.Builder()
                .url(REQUEST_URL)//request url
                .addHeader("AUTHTOKEN", AUTHTOKEN)
                .addHeader("HMAC", HmacSHA256.sha256_HMAC(json, HMAC_SECRET))
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                CardManager.getInstance().finishPreActivity();
                Log.d(TAG, "onFailure() called with: call = [" + call + "], e = [" + e + "]");
                bundle.putString("result", "failure");
                SendPacketResultActivity.actionStart(context, bundle);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                CardManager.getInstance().finishPreActivity();
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                //Log.d(TAG, "onResponse() called with: response.body().toString() : " + response.body().string());
                bundle.putString("result", response.body().string());
                SendPacketResultActivity.actionStart(context, bundle);
            }
        });

    }

}
