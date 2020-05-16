package com.standard.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * 发送数据之后的结果显示
 *
 * @author xukun
 * @version 1.0.0
 * @date 18-6-21
 */

public class SendPacketResultActivity extends Activity {

    private TextView mResultTvw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_packet_result);
        init();
    }

    private void init() {
//        mResultTvw = (TextView) findViewById(R.id.packet_result);
        Intent mIntent = getIntent();
        String result = mIntent.getStringExtra("result");
        if (result != null) {
            mResultTvw.setText(result);
        } else {
            mResultTvw.setText("error");
        }
    }

    public static void actionStart(Context context, Bundle bundle) {
        Intent intent = new Intent(context, SendPacketResultActivity.class);
        if (bundle != null) {
            String result = bundle.getString("result");
            intent.putExtra("result", result);
        }
        context.startActivity(intent);
    }
}
