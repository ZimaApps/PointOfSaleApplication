package com.standard.app.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.standard.app.PosApplication;
import com.standard.app.R;
import com.standard.app.Utils;
import com.standard.app.settings.SettingsActivity;
import com.standard.app.util.PacketProcessUtils;


public class LoginActivity extends Activity implements View.OnClickListener{
    private static final String TAG = Utils.TAGPUBLIC + LoginActivity.class.getSimpleName();

    private EditText mEditUserId;
    private EditText mEditPW;
    private TextView mTextSvn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        setContentView(R.layout.activity_login);
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(R.string.log_in);

        mEditUserId = (EditText) findViewById(R.id.user_id);
        mEditPW = (EditText) findViewById(R.id.user_password);
        mEditPW.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mEditPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString();
                if (mEditUserId.getText().toString().equals(Utils.TEST_managerId)) {
                    if (temp.length() > 8) {
                        mEditPW.setText(temp.substring(0, temp.length()-1));
                        mEditPW.setSelection(temp.length()-1);
                    }
                } else {
                    if (temp.length() > 4) {
                        mEditPW.setText(temp.substring(0, temp.length()-1));
                        mEditPW.setSelection(temp.length()-1);
                    }
                }
            }
        });

        mTextSvn = (TextView) findViewById(R.id.svn_id);
        setVersion();

        PosApplication.getApp().getDeviceManager();
    }

    private void setVersion() {
        Log.i(TAG, "getServiceVersion");
        String verName = null;
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            if (pi != null) {
                verName = pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mTextSvn.setText(verName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.log_in:
                if (mEditUserId.getText().toString().equals(Utils.TEST_operatorId) &&
                        mEditPW.getText().toString().equals(Utils.TEST_operatorPassword)) {
                    Intent intent = new Intent(this, PacketProcessActivity.class);
                    intent.putExtra(PacketProcessUtils.PACKET_PROCESS_TYPE, PacketProcessUtils.PACKET_PROCESS_SIGN_UP);
                    intent.putExtra("is_to_home_page", true);
                    startActivity(intent);
                } else if (mEditUserId.getText().toString().equals(Utils.TEST_managerId) &&
                        mEditPW.getText().toString().equals(Utils.TEST_managerPassword)) {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                } else if (mEditUserId.getText().toString().equals(Utils.TEST_operatorId_fork) &&
                        mEditPW.getText().toString().equals(Utils.TEST_operatorPassword)) {
                    Intent intent = new Intent(this, CaiMiActivity.class);
                    startActivity(intent);
                } else {
                    if (!mEditUserId.getText().toString().equals(Utils.TEST_operatorId) &&
                            !mEditUserId.getText().toString().equals(Utils.TEST_managerId)) {
                        Toast.makeText(this, getString(R.string.log_in_error_id), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.log_in_error_password), Toast.LENGTH_SHORT).show();
                    }
                }
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
