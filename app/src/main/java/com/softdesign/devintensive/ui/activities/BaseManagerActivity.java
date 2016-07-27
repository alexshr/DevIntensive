package com.softdesign.devintensive.ui.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.ui.fragments.LoginFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * умеет только выводить неудачный диалог(
 */
public abstract class BaseManagerActivity extends AppCompatActivity implements LoginFragment.OnSubmitListener {

    public static final long SPLASH_DURATION = 2000L;

    //messages
    public static final String MES_NETWORK_NOT_AVAILABLE = "MES_NETWORK_NOT_AVAILABLE";
    public static final String MES_USER_NOT_AUTHORIZED = "MES_USER_NOT_AUTHORIZED";
    public static final String MES_RESPONSE_ERROR = "MES_RESPONSE_ERROR";
    public static final String MES_SERVER_ERROR = "MES_SERVER_ERROR";
    public static final String MES_LOGIN_OR_PASSWORD_INCORRECT = "MES_LOGIN_OR_PASSWORD_INCORRECT";

    //запросу на профайл всегда нужен логин и пароль (токен не интересен)
    //public static final String MES_LOGIN_OR_PASSWORD_ABSENT = "MES_LOGIN_OR_PASSWORD_ABSENT";

    //подтвержение того, что пользователь авторизован
    public static final String MES_AUTHORIZATED = "MES_AUTHORIZATED";

    //получение и сохранение данных из сети
    public static final String MES_DOWNLOAD_STARTED = "MES_DOWNLOAD_STARTED";
    public static final String MES_DOWNLOAD_FINISHED = "MES_DOWNLOAD_FINISHED";

    //отображение локальных данных
    public static final String MES_SHOW_STARTED = "MES_SHOW_STARTED";
    public static final String MES_SHOW_FINISHED = "MES_SHOW_FINISHED";

    private static final String MES_LOGOUT = "MES_LOGOUT";


    private static final String TAG_LOGIN_FRAGMENT = "TAG_LOGIN_FRAGMENT";
    protected ProgressDialog mProgressDialog;

    private LoginFragment mLoginFragment;
    private PreferencesManager mPrefManager;

    private Handler uiHandler = new Handler();


    //стартует ли впервые - для splash
    private static boolean sIsFirstStart = true;

    //нужно ли проверять авторизацию
    private static boolean sIsAuthChecked;


    private static String LOG_TAG = ConstantManager.LOG_TAG + "_BaseActivity";

    //чтобы не перекрывать slash прогрессом
    private boolean mIsSlashNow;


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(LOG_TAG, "onMessageEvent " + event.mes);

        switch (event.mes) {
            case MES_USER_NOT_AUTHORIZED:
            case MES_LOGOUT:

                //удаляем токен,логин..
                mPrefManager.removeAuth();
                sIsAuthChecked = false;
                //показываем диалог авторизации
                showLoginDialog();
                hideProgress();

                //чтобы не мигал фон плавно убираем splash (если есть)
                //на форме автор. фон как у splash
                uiHandler.postDelayed(new Runnable() {
                    //убираем splash c задержкой, чтобы не мигало при смене экрана
                    @Override
                    public void run() {
                        hideSplash();
                    }
                }, 300);

                break;

            case MES_LOGIN_OR_PASSWORD_INCORRECT:
                //показываем ошибку на форме авториз.
                showLoginDialog();
                showError(getString(R.string.error_login_password));
                break;

            case MES_AUTHORIZATED:
                sIsAuthChecked = true;
                hideSplash();
                dismissLoginFragment();
                break;

            case MES_DOWNLOAD_STARTED:
                showProgress();
                break;

            case MES_DOWNLOAD_FINISHED:
                showData();
                showInfoMes(getString(R.string.success_refresh));
                break;

            case MES_NETWORK_NOT_AVAILABLE:
                hideProgress();
                showError(getString(R.string.error_no_network));
                showData();
                break;

            case MES_RESPONSE_ERROR:
            case MES_SERVER_ERROR:
                hideProgress();
                showError(getString(R.string.error_server));
                showData();
                break;

            case MES_SHOW_STARTED:
                showProgress();
                break;

            case MES_SHOW_FINISHED:

                hideProgress();
                break;

        }
    }


    private void showError(String mes) {
        if (mLoginFragment == null) {
            showErrorMes(mes);
        } else {
            //форма логина блокирует экран - сообщения туда выводим
            mLoginFragment.showError(mes);

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
        Log.d(LOG_TAG, "onStart started sIsFirstStart=" + sIsFirstStart + " sIsAuthChecked=" + sIsAuthChecked);

        long delay = 0;
        if (sIsFirstStart) {

            //держим splash
            showSplash();
            delay = SPLASH_DURATION;
            //не убираем тут splash,чтобы был не мигал белый экран при появлении формы авторизации
            //уберем либо при появлении экрана авторизации,
            //либо когда будет ясно, что авторизацияне потребуется
        }
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mPrefManager.hasTokenOrLogin()) {
                    postEvent(MES_USER_NOT_AUTHORIZED);
                } else {
                    //загрузка с сервера только если нужно проверить автор.
                    //пользователь обновляет данные по кнопке меню
                    //и нужно сделать, чтобы сервис регулярно обновлял лок базу
                    if (sIsAuthChecked) {
                        showData();
                    } else {
                        downloadData();
                    }
                }
                sIsFirstStart = false;
            }
        }, delay);
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
        postEvent(MES_DOWNLOAD_STARTED);
    }



    //отображаем локальные данные
    public void showData() {
        if (sIsAuthChecked) {
            postEvent(MES_SHOW_STARTED);
        } else {
            postEvent(MES_USER_NOT_AUTHORIZED);
        }
    }

    //сообщаем об ошибке
    public abstract void showErrorMes(String mes);

    //сообщаем об успехах
    public abstract void showInfoMes(String mes);

    //проверка наличия локальной даты
    //public abstract boolean hasLocalData();


    /**
     * submit на форме ввода логина
     */
    @Override
    public void onSubmit() {
        //загружаем, чтобы проверить авторизацию
        downloadData();
    }


    public void showLoginDialog() {
        Log.d(LOG_TAG, "showLoginDialog mLoginFragment=" + mLoginFragment);

        if (mLoginFragment == null) {
            mLoginFragment = new LoginFragment();
            mLoginFragment.show(getSupportFragmentManager(), TAG_LOGIN_FRAGMENT);
        }
        hideProgress();

        //выделяем пункт меню текущей активности; до этого стояло на logout
        checkCurrentDrawerMenuItem();
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
        if (!isSlashNow()) {
            //slash экран не перекрываем
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this, R.style.progress_screen);
                mProgressDialog.setCancelable(true);
                mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            }
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.progress_screen);
        }
    }

    public void hideProgress() {
        Log.d(LOG_TAG, "hideProgress()");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    public void checkCurrentDrawerMenuItem() {
    }

    public void showSplash() {
        setIsSlashNow(true);
    }


    public void hideSplash() {
        setIsSlashNow(false);
    }


    public void logout() {
        postEvent(MES_LOGOUT);
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

    public void setIsSlashNow(boolean isSlashNow) {
        mIsSlashNow = isSlashNow;
    }

    public boolean isSlashNow() {
        return mIsSlashNow;
    }


}
