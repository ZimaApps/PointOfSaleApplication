package com.zimaaps.bcx;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.standard.app.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide(); //<< this

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if(checkconnection()){
                            // your code here
                            Intent myIntent = new Intent(Splash.this, MainActivity.class);
                            Splash.this.startActivity(myIntent);
                        }else{
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    TextView zappname = (TextView) findViewById(R.id.zappname);
                                    zappname.setText("Tafadhali washa data.");
                                }
                            });

                            //Toast.makeText(Splash.this, "Tafadhali washa data.", Toast.LENGTH_LONG).show();
                        }

                    }
                },
                3000
        );
    }

    public boolean checkconnection(){
        return true;
    }





}
