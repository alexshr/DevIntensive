package com.softdesign.devintensive.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadDataService extends IntentService {
    //messages
    public static final String MES_NETWORK_NOT_AVAILABLE = "MES_NETWORK_NOT_AVAILABLE";
    public static final String MES_DATA_LOADED = "MES_DATA_LOADED";

    public static final String MES_USER_NOT_AUTHORIZED = "MES_USER_NOT_AUTHORIZED";
    public static final String MES_RESPONSE_ERROR = "MES_RESPONSE_ERROR";
    public static final String MES_SERVER_ERROR = "MES_SERVER_ERROR";
    public static final String MES_LOGIN_OR_PASSWORD_INCORRECT = "MES_LOGIN_OR_PASSWORD_INCORRECT";
    public static final String MES_LOGIN_OR_PASSWORD_ABSENT = "MES_LOGIN_OR_PASSWORD_ABSENT";
    public static final String MES_AUTHORIZATED = "MES_AUTHORIZATED";


    String LOG_TAG = ConstantManager.LOG_TAG;

    private static final String ACTION_USER_LIST = "USER_LIST";
    private static final String ACTION_TOKEN_AND_PROFILE = "TOKEN_AND_PROFILE";

    private static final String PARAM_LOGIN = "LOGIN";
    private static final String PARAM_PASS = "PAS";

    private DataManager mDataManager = DataManager.getInstance();
    private RepositoryDao mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();
    private UserDao mUserDao = mDataManager.getDaoSession().getUserDao();
    private PreferencesManager mPrefManager = mDataManager.getPreferencesManager();


    public DownloadDataService() {
        super("DownloadDataService");
    }


    /**
     * стартуем загрузку списка пользователь
     *
     * @see IntentService
     */

    public static void startActionUserList(Context context) {
        Intent intent = new Intent(context, DownloadDataService.class);
        intent.setAction(ACTION_USER_LIST);
        context.startService(intent);
    }

    /**
     * стартуем получение токена и профиля
     *
     * @see IntentService
     */

    public static void startActionTokenAndProfile(Context context, String login, String pass) {
        Intent intent = new Intent(context, DownloadDataService.class);
        intent.setAction(ACTION_TOKEN_AND_PROFILE);
        intent.putExtra(PARAM_LOGIN, login);
        intent.putExtra(PARAM_PASS, pass);
        context.startService(intent);
    }

    /**
     * стартуем сначала авториз а потом загрузку списка
     *
     * @see IntentService
     */

    public static void startActionFull(Context context, String login, String pass) {
        Intent intent = new Intent(context, DownloadDataService.class);
        intent.setAction(ACTION_USER_LIST);
        intent.putExtra(PARAM_LOGIN, login);
        intent.putExtra(PARAM_PASS, pass);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            String login = intent.getStringExtra(PARAM_LOGIN);
            String pass = intent.getStringExtra(PARAM_PASS);

            if (ACTION_TOKEN_AND_PROFILE.equals(action)) {
                //идем за токеном и профилем
                if (loadTokenAndProfile(login, pass)) {
                    EventBus.getDefault().post(new MessageEvent(MES_AUTHORIZATED));
                    EventBus.getDefault().post(new MessageEvent(MES_DATA_LOADED));
                }
            } else if (ACTION_USER_LIST.equals(action)) {
                if (login == null) {
                    //пароль не прислали - значит сразу идем за списком
                    if (loadUserListFromServerAndSaveInDb()) {
                        EventBus.getDefault().post(new MessageEvent(MES_DATA_LOADED));
                    }
                } else {
                    //прислали пароль - сначала авторизуемся
                    if (loadTokenAndProfile(login, pass)) {
                        EventBus.getDefault().post(new MessageEvent(MES_AUTHORIZATED));
                        if (loadUserListFromServerAndSaveInDb()) {
                            EventBus.getDefault().post(new MessageEvent(MES_DATA_LOADED));
                        }
                    }
                }
            }
        }
    }


    private boolean loadTokenAndProfile(String login, String pass) {

        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            EventBus.getDefault().post(new MessageEvent(MES_NETWORK_NOT_AVAILABLE));
            return false;
        }


        Call<UserModelRes> call = mDataManager.loginUser(
                new UserLoginReq(login, pass));
        // synchr call
        try {
            Response<UserModelRes> response = call.execute();


            if (response.code() == 200) {
                saveTokenAndProfile(response.body(), login, pass);
                return true;

            } else {

                if (response.code() == 404) {
                    EventBus.getDefault().post(new MessageEvent(MES_LOGIN_OR_PASSWORD_INCORRECT));
                } else {

                    EventBus.getDefault().post(new MessageEvent(MES_RESPONSE_ERROR));
                }
                return false;
            }
        } catch (IOException e) {
            EventBus.getDefault().post(new MessageEvent(MES_SERVER_ERROR));
            return false;
        }

    }


    /**
     * загружаем с сервера список пользователей и записываем в бд
     */
    private boolean loadUserListFromServerAndSaveInDb() {


        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            EventBus.getDefault().post(new MessageEvent(MES_NETWORK_NOT_AVAILABLE));
            return false;
        }

        Call<UserListRes> call = mDataManager.getUserListFromNetwork();
        try {
            Response<UserListRes> response = call.execute();
            if (response.code() == 200) {

                List<Repository> allRepositories = new ArrayList<>();
                List<User> allUsers = new ArrayList<>();

                for (UserListRes.UserData userRes : response.body().getData()) {
                    allRepositories.addAll(getRepoListFromUserRes(userRes));
                    allUsers.add(new User(userRes));
                }

                mRepositoryDao.insertOrReplaceInTx(allRepositories);//вставляем пакетом!
                mUserDao.insertOrReplaceInTx(allUsers);


                return true;

            } else if (response.code() == 401) {
                EventBus.getDefault().post(new MessageEvent(MES_USER_NOT_AUTHORIZED));
                return false;
            } else {
                Log.e(LOG_TAG, "Network error: " + response.message());
                EventBus.getDefault().post(new MessageEvent(MES_RESPONSE_ERROR));
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Network failure: ", e);
            EventBus.getDefault().post(new MessageEvent(MES_SERVER_ERROR));
            return false;
        }
    }

    /**
     * cобираем репозитории для вставки в бд
     *
     * @param userData
     * @return
     */
    private List<Repository> getRepoListFromUserRes(UserListRes.UserData userData) {
        final String userId = userData.getId();

        List<Repository> repositories = new ArrayList<>();
        for (UserModelRes.Repo repositoryRes : userData.getRepositories().getRepo()) {
            repositories.add(new Repository(repositoryRes, userId));
        }

        return repositories;
    }

    /**
     * кладем токен и данные профиля в Preferences
     *
     * @param userModel
     * @param login
     * @param pass
     */
    private void saveTokenAndProfile(UserModelRes userModel, String login, String pass) {

        mPrefManager.saveLogin(login);
        mPrefManager.savePassword(pass);
        mPrefManager.saveUserId(userModel.getData().getUser().getId());
        mPrefManager.saveAuthToken(userModel.getData().getToken());

        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };

        mPrefManager.saveUserProfileValues(userValues);

        List<String> userFields = new ArrayList<>();
        userFields.add(userModel.getData().getUser().getContacts().getPhone());
        userFields.add(userModel.getData().getUser().getContacts().getEmail());
        userFields.add(userModel.getData().getUser().getContacts().getVk());
        userFields.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        String bio = userModel.getData().getUser().getPublicInfo().getBio();
        userFields.add(bio.isEmpty() ? "" : bio);

        mPrefManager.saveUserProfileData(userFields);

        String[] userNames = {
                userModel.getData().getUser().getFirstName(),
                userModel.getData().getUser().getSecondName()
        };

        mPrefManager.saveUserName(userNames);

        mPrefManager.savePhotoUri(userModel.getData().getUser().getPublicInfo().getPhoto());
        mPrefManager.saveAvatarUri(userModel.getData().getUser().getPublicInfo().getAvatar());

    }

}
