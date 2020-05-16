package com.standard.app.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.activity.PacketProcessActivity;
import com.standard.app.util.PacketProcessUtils;

public class OnlineInitActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + OnlineInitActivity.class.getSimpleName();

    private Button mBtnOk;
    private EditText mSecurePassword;
    private EditText mInitPassword;
    private TextView mErrorInfo;

    private boolean mIsSecurePasswordOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_init);

        mBtnOk = (Button) findViewById(R.id.btn_online_init_ok);
        mSecurePassword = (EditText) findViewById(R.id.online_init_secure_password);
        mInitPassword = (EditText) findViewById(R.id.online_init_init_password);
        mErrorInfo = (TextView) findViewById(R.id.error_info);

        mSecurePassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mInitPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mSecurePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                Log.i(TAG, "mInitPassword.isEnabled() = "+mInitPassword.isEnabled() + ", str = " + str);
                if (mSecurePassword.isEnabled()) {
                    if (str != null && (str.length() == 8)) {
                        mBtnOk.setEnabled(true);
                    } else {
                        mBtnOk.setEnabled(false);
                    }
                }
            }
        });

        mInitPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                Log.i(TAG, "mInitPassword.isEnabled() = "+mInitPassword.isEnabled() + ", str = " + str);
                if (mInitPassword.isEnabled()) {
                    if (str != null && (str.length() == 8)) {
                        mBtnOk.setEnabled(true);
                    } else {
                        mBtnOk.setEnabled(false);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_online_init_cancle:
                finish();
                break;
            case R.id.btn_online_init_ok:
                Log.i(TAG, "confirm init password");
                if (!mIsSecurePasswordOK) {
                    Log.i(TAG, "confirm secure password");
                    String strSecu = mSecurePassword.getText().toString();
                    if (strSecu.equals(Utils.TEST_online_secure_password)) {
                        mIsSecurePasswordOK = true;
                        mErrorInfo.setText(null);
                        mBtnOk.setEnabled(false);
                        mSecurePassword.setEnabled(false);
                        mInitPassword.setEnabled(true);
                        mInitPassword.requestFocus();
                    } else {
                        mErrorInfo.setText(R.string.input_secure_password_error);
                        mSecurePassword.setText(null);
                    }
                } else {
                    Log.i(TAG, "confirm init password");
                    String strSecu = mInitPassword.getText().toString();
                    if (strSecu.equals(Utils.TEST_online_init_password)) {
                        mErrorInfo.setText(null);
                        Intent intent = new Intent(this, PacketProcessActivity.class);
                        intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_ONLINE_INIT);
                        startActivity(intent);
                        finish();
                    } else {
                        mErrorInfo.setText(R.string.input_init_password_error);
                        mInitPassword.setText(null);
                    }
                }
                break;
        }
    }
}
