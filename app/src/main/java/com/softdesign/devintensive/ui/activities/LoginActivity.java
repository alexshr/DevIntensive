package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.MessageEvent;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String LOG_TAG = ConstantManager.LOG_TAG + "_AuthActivity";


    private static final String NETWORK_NOT_AVAILABLE = "NETWORK_NOT_AVAILABLE";
    private static final String USERLIST_LOADED_AND_SAVED = "USERLIST_LOADED_AND_SAVED";
    private static final String USER_NOT_AUTHORIZED = "USER_NOT_AUTHORIZED";
    private static final String RESPONSE_ERROR = "RESPONSE_ERROR";
    private static final String SERVER_ERROR = "SERVER_ERROR";
    private static final String LOGIN_OR_PASSWORD_INCORRECT = "LOGIN_OR_PASSWORD_INCORRECT";
    private static final String AUTH_TOKEN_RECEIVED = "AUTH_TOKEN_RECEIVED";
    //private static final String SHOW_SPLASH = "SHOW_SPLASH";
    //private static final String SHOW_PROGRESS = "SHOW_PROGRESS";


    private DataManager mDataManager;
    private PreferencesManager mPrefManager;

    private RepositoryDao mRepositoryDao;
    private UserDao mUserDao;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.login)
    EditText mLogin;
    @BindView(R.id.email_layout)
    TextInputLayout mEmailLayout;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.pass_layout)
    TextInputLayout mPassLayout;

    @BindView(R.id.progress)
    ProgressBar mProgressBar;

    @BindView(R.id.auth_layout)
    RelativeLayout mAuthLayout;


    @OnClick({R.id.remember, R.id.auth_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auth_button:
                login();
                break;
            case R.id.remember:
                rememberPassword();
                break;
        }
    }

    //event bus receiver
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        switch (event.mes) {
            case NETWORK_NOT_AVAILABLE:
                hideSplash();
                hideProgress();

                //есть еще вариант проверить есть в базе список и пойти туда
                startUserMainActivity();
                finish();

                break;

            case USERLIST_LOADED_AND_SAVED:
                hideSplash();
                hideProgress();
                startUserListActivity();
                finish();

                break;

            case USER_NOT_AUTHORIZED:


                if (mPrefManager.getLogin().length() > 0) {
                    //не будем заставлять пользователя авторизоваться руками
                    //возможно просто сессия закончилась (токен перестал действовать)
                    login();
                } else {
                    hideSplash();
                    //показываем форму авторизации
                    showLoginLayout();
                    showSnackbar("Необходима авторизация");
                }


                break;

            case AUTH_TOKEN_RECEIVED:
                loadUserListFromServerAndSaveInDbOnBackground();

                break;

            case LOGIN_OR_PASSWORD_INCORRECT:
                hideProgress();
                showSnackbar("Неверный логин или пароль");
                mPrefManager.removeAuth();
                showLoginLayout();
                break;

            case RESPONSE_ERROR:
                hideSplash();
                hideProgress();
                startUserMainActivity();

                break;

            case SERVER_ERROR:
                hideSplash();
                hideProgress();
                startUserMainActivity();

                break;


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        mAuthLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        mDataManager = DataManager.getInstance();
        mPrefManager = mDataManager.getPreferencesManager();

        mUserDao = mDataManager.getDaoSession().getUserDao();
        mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();

        showSplash();

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        //сразу проверяем нужно ли идти на авторизацию
        if(mPrefManager.getAuthToken().isEmpty()){
            EventBus.getDefault().post(new MessageEvent(USER_NOT_AUTHORIZED));
        }else {
            loadUserListFromServerAndSaveInDbOnBackground();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void showLoginLayout() {
        mAuthLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }



    private void login() {
        showProgress();

        if (NetworkStatusChecker.isNetworkAvailable(this)) {


            //если в преф. хранится логин, то тут идет автологин,
            //и поля не заполнены
            String login = mLogin.getText().toString();
            String pass = mPassword.getText().toString();
            if (mPrefManager.getLogin().length() > 0) {
                login = mPrefManager.getLogin();
                pass = mPrefManager.getPassword();
            }

            Call<UserModelRes> call = mDataManager.loginUser(
                    new UserLoginReq(login, pass));
            // asynchr call
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        saveTokenAndProfile(response.body());
                        EventBus.getDefault().post(new MessageEvent(AUTH_TOKEN_RECEIVED));
                    } else {
                        //неверный пароль не храним
                        mPrefManager.removeAuth();
                        if (response.code() == 404) {
                            EventBus.getDefault().post(new MessageEvent(LOGIN_OR_PASSWORD_INCORRECT));
                        } else {
                            mPrefManager.removeAuth();
                            EventBus.getDefault().post(new MessageEvent(RESPONSE_ERROR));
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    // TODO: handle error
                    mPrefManager.removeAuth();
                    EventBus.getDefault().post(new MessageEvent(SERVER_ERROR));
                }
            });

        } else {
            showSnackbar(getString(R.string.error_no_network));
        }
    }

    private void saveTokenAndProfile(UserModelRes userModel) {

        mPrefManager.saveUserId(userModel.getData().getUser().getId());
        mPrefManager.saveAuthToken(userModel.getData().getToken());

        if (mPrefManager.getLogin().isEmpty()) {
            mPrefManager.saveLogin(mLogin.getText().toString());
            mPrefManager.savePassword(mPassword.getText().toString());
        }

        saveUserValues(userModel);
        saveUserData(userModel);
        saveUserName(userModel);
        saveImagesUrls(userModel);
        finish();
    }

    private void saveImagesUrls(UserModelRes userModel) {

        mPrefManager.savePhotoUri(userModel.getData().getUser().getPublicInfo().getPhoto());
        mPrefManager.saveAvatarUri(userModel.getData().getUser().getPublicInfo().getAvatar());


    }


    /**
     * save user values (rating ect.) from rest response model to Shared Preferences
     */
    private void saveUserValues(UserModelRes userModel) {
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };

        mPrefManager.saveUserProfileValues(userValues);
    }

    /**
     * save user info (phone, email ect.) from rest response model to Shared Preferences
     */
    private void saveUserData(UserModelRes userModel) {
        List<String> userFields = new ArrayList<>();
        userFields.add(userModel.getData().getUser().getContacts().getPhone());
        userFields.add(userModel.getData().getUser().getContacts().getEmail());
        userFields.add(userModel.getData().getUser().getContacts().getVk());
        userFields.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        String bio = userModel.getData().getUser().getPublicInfo().getBio();
        userFields.add(bio.isEmpty() ? "" : bio);

        mPrefManager.saveUserProfileData(userFields);
    }

    /**
     * save user name from rest response model to Shared Preferences
     */
    private void saveUserName(UserModelRes userModel) {
        String[] userNames = {
                userModel.getData().getUser().getFirstName(),
                userModel.getData().getUser().getSecondName()
        };

        mPrefManager.saveUserName(userNames);
    }

    //cобираем репозитории для вставки в бд
    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }

    //TODO
    //не понял чем тут поможет пауза из примера
    private void loadUserListFromServerAndSaveInDbOnBackground() {
        loadUserListFromServerAndSaveInDb();
        /*не понял чем тут поможет обычная пауза в ui потоке??
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadUserListFromServerSaveInDb();
            }
        }, AppConfig.START_DELAY);*/
    }

    private void loadUserListFromServerAndSaveInDb() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            EventBus.getDefault().post(new MessageEvent(NETWORK_NOT_AVAILABLE));
            return;
        }

        Call<UserListRes> call = mDataManager.getUserListFromNetwork();
        call.enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                try {
                    if (response.code() == 200) {

                        List<Repository> allRepositories = new ArrayList<>();
                        List<User> allUsers = new ArrayList<>();

                        for (UserListRes.UserData userRes : response.body().getData()) {
                            allRepositories.addAll(getRepoListFromUserRes(userRes));
                            allUsers.add(new User(userRes));
                        }

                        mRepositoryDao.insertOrReplaceInTx(allRepositories);//вставляем пакетом!
                        mUserDao.insertOrReplaceInTx(allUsers);

                        EventBus.getDefault().post(new MessageEvent(USERLIST_LOADED_AND_SAVED));

                    } else if (response.code() == 401) {
                        EventBus.getDefault().post(new MessageEvent(USER_NOT_AUTHORIZED));

                    } else {
                        Log.e(LOG_TAG, "Network error: " + response.message());
                        EventBus.getDefault().post(new MessageEvent(RESPONSE_ERROR));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    showSnackbar("Что-то пошло не так");
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                Log.e(LOG_TAG, "Network failure: " + t.getMessage());
                EventBus.getDefault().post(new MessageEvent(SERVER_ERROR));
            }
        });
    }


    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }


    private void startUserMainActivity() {
        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivity(loginIntent);
    }

    private void startUserListActivity() {
        Intent loginIntent = new Intent(this, UserListActivity.class);
        startActivity(loginIntent);
    }
}