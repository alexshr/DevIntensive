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
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.ui.fragments.LoginFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.softdesign.devintensive.services.DownloadDataService.MES_AUTHORIZATED;
import static com.softdesign.devintensive.services.DownloadDataService.MES_DATA_LOADED;
import static com.softdesign.devintensive.services.DownloadDataService.MES_LOGIN_OR_PASSWORD_ABSENT;
import static com.softdesign.devintensive.services.DownloadDataService.MES_LOGIN_OR_PASSWORD_INCORRECT;
import static com.softdesign.devintensive.services.DownloadDataService.MES_NETWORK_NOT_AVAILABLE;
import static com.softdesign.devintensive.services.DownloadDataService.MES_RESPONSE_ERROR;
import static com.softdesign.devintensive.services.DownloadDataService.MES_SERVER_ERROR;
import static com.softdesign.devintensive.services.DownloadDataService.MES_USER_NOT_AUTHORIZED;

/**
 * умеет только выводить неудачный диалог(
 */
public abstract class BaseManagerActivity extends AppCompatActivity implements LoginFragment.OnSubmitListener {
    private static final String TAG_LOGIN_FRAGMENT = "TAG_LOGIN_FRAGMENT";
    private static final String KEY_IS_AUTH_CHECKED = "KEY_IS_AUTH_CHECKED";
    protected ProgressDialog mProgressDialog;

    private LoginFragment mLoginFragment;
    private PreferencesManager mPrefManager;

    //флаг проверки авторизации
    private static boolean sIsUserChecked;

    private String LOG_TAG = ConstantManager.LOG_TAG + "_BaseActivity";


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(LOG_TAG, "onMessageEvent " + event.mes);

        if (event.mes == MES_USER_NOT_AUTHORIZED
                || event.mes == MES_LOGIN_OR_PASSWORD_ABSENT
                || event.mes == MES_LOGIN_OR_PASSWORD_INCORRECT
                ) {

            //удаляем токен,логин..
            mPrefManager.removeTokenAndKey();
            mPrefManager.removeLoginAndPass();

            //показываем диалог авторизации
            hideProgress();
            showLoginDialog();

            //сообщение об ошибке логина
            if (event.mes == MES_LOGIN_OR_PASSWORD_INCORRECT) {
                showLoginError();
            }

        } else {

            //закрываем форму ввода
            if (event.mes == MES_AUTHORIZATED
                    || event.mes == MES_DATA_LOADED) {
                dismissLoginFragment();
                sIsUserChecked = true;
            }

            if (event.mes == MES_DATA_LOADED) {
                hideSplash();
                hideProgress();
                showInfo("данные успешно обновлены с сервера");
            }

            //отображаем локальные данные - от сети взяли что могли
            if (event.mes == MES_NETWORK_NOT_AVAILABLE
                    || event.mes == MES_RESPONSE_ERROR
                    || event.mes == MES_SERVER_ERROR
                    || event.mes == MES_DATA_LOADED) {
                showData();
            }

            //show error
            if (event.mes == MES_NETWORK_NOT_AVAILABLE) {
                showError(getResources().getString(R.string.error_no_network));
            } else if (event.mes == MES_RESPONSE_ERROR) {
                showError(getResources().getString(R.string.error_server));
            } else if (event.mes == MES_SERVER_ERROR) {
                showError(getResources().getString(R.string.error_server));
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate started");
        mPrefManager = DataManager.getInstance().getPreferencesManager();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Log.v(LOG_TAG, "onPause started mIsAuthChecked=" + sIsUserChecked);

        if (!sIsUserChecked) {

            //чтобы проверить авторизацию заодно загрузим свежие данные
            //пока не подтвердится авторизация - пользователь лок данных не увидит
            downloadData();

        } else {
            //смотрим локальные данные
            //теперь на сервер - только по refresh  ользователя
            showData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop started");
        EventBus.getDefault().unregister(this);
    }

    public void postEvent(String mes) {
        Log.d(LOG_TAG, "postEvent " + mes);
        EventBus.getDefault().post(new MessageEvent(mes));

    }

    //загружаем данные с сервера и сохраняем локально
    public void downloadData() {
        if (sIsUserChecked) {
            showProgress();
        } else {
            //это всегда будет при входе
            showSplash();
        }
    }

    //отображаем локальные данные
    public abstract void showData();

    //сообщаем об ошибке
    public abstract void showError(String mes);

    //сообщаем об успехах
    public abstract void showInfo(String mes);


    /**
     * submit на форме ввода логина
     */
    @Override
    public void onSubmit() {
        //dismissLoginFragment();
        downloadData();
    }


    public void showLoginDialog() {
        Log.d(LOG_TAG, "showLoginDialog mLoginFragment=" + mLoginFragment);

        if (mLoginFragment == null) {
            mLoginFragment = new LoginFragment();
            mLoginFragment.show(getSupportFragmentManager(), TAG_LOGIN_FRAGMENT);
        }
    }

    public void showLoginError() {
        Log.d(LOG_TAG, "showLoginError mLoginFragment=" + mLoginFragment);

        if (mLoginFragment != null) {
            mLoginFragment.showError();
        }
    }


    public void dismissLoginFragment() {
        Log.d(LOG_TAG, "dismissLoginFragment mLoginFragment=" + mLoginFragment);
        if (mLoginFragment != null) {
            mLoginFragment.dismiss();
            mLoginFragment = null;
        }
    }


    //worry about blocking activities home and back buttons!!!
    public void showProgress() {
        Log.d(LOG_TAG, "showProgress()");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.progress_screen);
            mProgressDialog.setCancelable(true);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.progress_screen);

    }

    public void showSplash() {
        Log.d(LOG_TAG, "showSplash()");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.progress_screen);
            mProgressDialog.setCancelable(false);
//            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.splash_screen);
    }


    public void hideProgress() {
        Log.d(LOG_TAG, "hideProgress()");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void hideSplash() {
        Log.d(LOG_TAG, "hideSplash()");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void runWithDelay() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        }, 5000);
    }


    public void logout() {
        DataManager.getInstance().getPreferencesManager().removeTokenAndKey();
        DataManager.getInstance().getPreferencesManager().removeLoginAndPass();
        //обновляем с сервера, на форму авторизации отправит сразу автоматом
        downloadData();
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
