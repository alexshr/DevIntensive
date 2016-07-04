package com.softdesign.devintensive.ui.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;


public class BaseActivity extends AppCompatActivity  {
    private static final String LOG_TAG = ConstantManager.LOG_TAG;
    protected ProgressDialog mProgressDialog;

    public void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.layout.progress_splash);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.progress_splash);
        } else {
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.progress_splash);
        }
        runWithDelay();
    }

    public void hideProgress() {
        mProgressDialog.hide();
    }

    public void showError(String message, Exception error) {
        showToast(message);
        Log.e(LOG_TAG, String.valueOf(error));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void runWithDelay(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        }, 5000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate started");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop started");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy started");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume started");

    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.v(LOG_TAG, "onPause started");
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.v(LOG_TAG, "onStart started");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(LOG_TAG, "onRestart started");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "onSaveInstanceState started");

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(LOG_TAG, "onRestoreInstanceState started");

    }

}
