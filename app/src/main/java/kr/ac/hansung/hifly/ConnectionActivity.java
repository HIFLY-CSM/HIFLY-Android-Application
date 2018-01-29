package kr.ac.hansung.hifly;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import dji.common.battery.BatteryState;
import dji.sdk.base.BaseProduct;

public class ConnectionActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ConnectionActivity.class.getName();

    //Product
    private TextView mTextProduct;
    private ProgressBar mProgressBar;
    private TextView mTextBatteryPercent;
    private Button mBtnOpen;
    private TextView mTextBattery;
    private LinearLayout mLinearLayoutBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }
        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_connection);

        initUI();

        //Register the broadcast receiver for receiving the device connection's changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectionApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    /*
    **** UI & sign in process
    */
    public void initUI() {
        //Product
        mTextProduct = (TextView)findViewById(R.id.model);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mTextBatteryPercent = (TextView)findViewById(R.id.battery);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mTextBattery = (TextView)findViewById(R.id.text);
        mLinearLayoutBattery = (LinearLayout) findViewById(R.id.battery_linear_layout);

        mBtnOpen.setOnClickListener(this);
        mBtnOpen.setEnabled(false);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct = ConnectionApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");
            mBtnOpen.setEnabled(true);
            if (null != mProduct.getModel()) {
                mTextProduct.setText(mProduct.getModel().getDisplayName());
                mProduct.getBattery().setStateCallback(new BatteryState.Callback() {
                    @Override
                    public void onUpdate(BatteryState batteryState) {
                        mProgressBar.setProgress(batteryState.getChargeRemainingInPercent());
                    }
                });
                mTextBatteryPercent.setText(Integer.toString(mProgressBar.getProgress())+"%");
                mLinearLayoutBattery.setVisibility(View.VISIBLE);
                mTextBattery.setVisibility(View.VISIBLE);
            } else {
                mBtnOpen.setEnabled(false);
                mTextProduct.setText(R.string.product_information);
                mProduct.getBattery().setStateCallback(null);
                mProgressBar.setProgress(0);
                mTextBatteryPercent.setText(Integer.toString(mProgressBar.getProgress())+"%");
                mLinearLayoutBattery.setVisibility(View.GONE);
                mTextBattery.setVisibility(View.GONE);
            }
        } else {
            Log.v(TAG, "refreshSDK: False");
            mBtnOpen.setEnabled(false);
            mTextProduct.setText(R.string.product_information);
            mProgressBar.setProgress(0);
            mTextBatteryPercent.setText(Integer.toString(mProgressBar.getProgress())+"%");
            mLinearLayoutBattery.setVisibility(View.GONE);
            mTextBattery.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }
}
