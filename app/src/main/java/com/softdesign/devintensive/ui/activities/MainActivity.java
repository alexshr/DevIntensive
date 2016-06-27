package com.softdesign.devintensive.ui.activities;

import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

public class MainActivity extends AppCompatActivity implements ConstantManager {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG,"onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG,"onStop started");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG,"onDestroy started");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.v(LOG_TAG,"onResume started");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG,"onPause started");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG,"onStart started");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.v(LOG_TAG,"onRestart started");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG,"onSaveInstanceState started");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG,"onRestoreInstanceState started");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
