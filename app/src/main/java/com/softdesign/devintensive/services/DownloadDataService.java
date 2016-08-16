package com.softdesign.devintensive.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserLikesRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.LikesBy;
import com.softdesign.devintensive.data.storage.models.LikesByDao;
import com.softdesign.devintensive.data.storage.models.Repository;
import com.softdesign.devintensive.data.storage.models.RepositoryDao;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.ui.activities.BaseManagerActivity;
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


    String LOG_TAG = ConstantManager.LOG_TAG;

    private static final String ACTION_USER_LIST = "USER_LIST";
    private static final String ACTION_TOKEN_AND_PROFILE = "TOKEN_AND_PROFILE";
    private static final String ACTION_LIKE = "LIKE";

    private static final String PARAM_USER_ID = "PARAM_USER_ID";
    private static final String PARAM_IS_LIKE = "PARAM_IS_LIKE";

    private static final String PARAM_LOGIN = "LOGIN";
    private static final String PARAM_PASS = "PAS";

    private DataManager mDataManager = DataManager.getInstance();
    private RepositoryDao mRepositoryDao = mDataManager.getDaoSession().getRepositoryDao();
    private LikesByDao mLikesByDao = mDataManager.getDaoSession().getLikesByDao();
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

    public static void startActionLike(Context context, String userId, boolean isLike) {
        Intent intent = new Intent(context, DownloadDataService.class);
        intent.setAction(ACTION_LIKE);
        intent.putExtra(PARAM_USER_ID, userId);
        intent.putExtra(PARAM_IS_LIKE, isLike);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            String login = intent.getStringExtra(PARAM_LOGIN);
            String pass = intent.getStringExtra(PARAM_PASS);

            if (checkNetwork()) {

                if (ACTION_TOKEN_AND_PROFILE.equals(action)) {
                    //идем за токеном и профилем

                    if (loadTokenAndProfile(login, pass)) {
                        EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_AUTHORIZATED));
                        EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_DOWNLOAD_FINISHED));
                    }

                } else if (ACTION_USER_LIST.equals(action)) {
                    if (login == null) {
                        //пароль не прислали - значит сразу идем за списком
                        if (loadUserListFromServerAndSaveInDb()) {
                            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_AUTHORIZATED));
                            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_DOWNLOAD_FINISHED));
                        }
                    } else {
                        //прислали пароль - сначала авторизуемся
                        if (loadTokenAndProfile(login, pass)) {
                            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_AUTHORIZATED));
                            if (loadUserListFromServerAndSaveInDb()) {
                                EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_DOWNLOAD_FINISHED));
                            }
                        }
                    }
                } else if (ACTION_LIKE.equals(action)) {
                    String userId = intent.getStringExtra(PARAM_USER_ID);
                    boolean isLike = intent.getBooleanExtra(PARAM_IS_LIKE, true);
                    if (likeToServer(userId, isLike)) {
                        //если получилось - сразу пишем в базу
                        likeToDb(userId, isLike);
                    } else {
                        //TODO отменить результат в ui
                        Log.e(LOG_TAG, "!!operation fails: likeToDb recId=" + userId + " isChecked=" + isLike);
                    }
                }
            }
        }
    }


    private void likeToDb(String recipientId, boolean isChecked) {
        LikesBy likesBy = new LikesBy(mPrefManager.getUserId(), recipientId);
try {
    if (isChecked) {
        mLikesByDao.insertOrReplace(likesBy);
        Log.e(LOG_TAG, "inserting: " + likesBy);
    } else {
        mLikesByDao.delete(likesBy);
        Log.e(LOG_TAG, "deleting: " + likesBy);
    }
}catch (Exception e){
    Log.e(LOG_TAG,",e");
}
}


    private boolean likeToServer(String userId, boolean isChecked) {

        Call<UserLikesRes> call = mDataManager.like(userId, isChecked);
        try {
            Response<UserLikesRes> response = call.execute();
            if (response.code() == 200) {

                boolean isSuccess = !isChecked;
                for (String senderId : response.body().getData().getLikesBy()) {
                    if (senderId .equals(mPrefManager.getUserId())) {
                        isSuccess = !isSuccess;
                        break;
                    }
                }
                Log.d(LOG_TAG, "likeToServer senderId=" + mPrefManager.getUserId()
                        + " recId=" + userId + " isChecked=" + isChecked + " isSuccess=" + isSuccess);

                return isSuccess;

            } else if (response.code() == 401) {
                EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_USER_NOT_AUTHORIZED));
                return false;
            } else {
                Log.e(LOG_TAG, "Network error: " + response.message());
                EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_RESPONSE_ERROR));
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Network failure: ", e);
            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_SERVER_ERROR));
            return false;
        }
    }

    private boolean loadTokenAndProfile(String login, String pass) {


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
                    EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_LOGIN_OR_PASSWORD_INCORRECT));
                } else {

                    EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_RESPONSE_ERROR));
                }
                return false;
            }
        } catch (IOException e) {
            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_SERVER_ERROR));
            return false;
        }

    }


    /**
     * загружаем с сервера список пользователей и записываем в бд
     */
    private boolean loadUserListFromServerAndSaveInDb() {

        Call<UserListRes> call = mDataManager.getUserListFromNetwork();
        try {
            Response<UserListRes> response = call.execute();
            if (response.code() == 200) {

                List<Repository> allRepositories = new ArrayList<>();
                List<LikesBy> allLikesBy = new ArrayList<>();
                List<User> allUsers = new ArrayList<>();

                for (UserListRes.UserData userRes : response.body().getData()) {
                    allRepositories.addAll(getRepoListFromUserRes(userRes));
                    allLikesBy.addAll(getLikesByListFromUserRes(userRes));

                    User user = new User(userRes);
                    allUsers.add(user);

                }

                mUserDao.insertOrReplaceInTx(allUsers);
                mRepositoryDao.insertOrReplaceInTx(allRepositories);
                mLikesByDao.insertOrReplaceInTx(allLikesBy);


                Log.d(LOG_TAG, "loadUserListFromServerAndSaveInDb users size=" + allUsers.size()
                        + " repos size=" + allRepositories.size() + " likesBy size=" + allLikesBy.size());

                return true;

            } else if (response.code() == 401) {
                EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_USER_NOT_AUTHORIZED));
                return false;
            } else {
                Log.e(LOG_TAG, "Network error: " + response.message());
                EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_RESPONSE_ERROR));
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Network failure: ", e);
            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_SERVER_ERROR));
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


    private List<LikesBy> getLikesByListFromUserRes(UserListRes.UserData userData) {
        final String recipientId = userData.getId();

        List<LikesBy> likesByList = new ArrayList<>();
        //Log.d(LOG_TAG, "getLikesByListFromUserRes called for user=" + userData.getId());
        for (String senderId : userData.getProfileValues().getLikesBy()) {
            LikesBy likesBy = new LikesBy(senderId, recipientId);
            Log.d(LOG_TAG, "getLikesByListFromUserRes: " + likesBy);
            likesByList.add(likesBy);

        }
        return likesByList;
    }

    /**
     * кладем токен и данные профиля в Preferences
     *
     * @param userModel
     * @param login
     * @param pass
     */

    private void saveTokenAndProfile(UserModelRes userModel, String login, String pass) {

        Log.d(LOG_TAG, "saveTokenAndProfile started");

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

    private boolean checkNetwork() {
        if (!NetworkStatusChecker.isNetworkAvailable(this)) {
            EventBus.getDefault().post(new MessageEvent(BaseManagerActivity.MES_NETWORK_NOT_AVAILABLE));
            return false;
        } else {
            return true;
        }
    }

}
