package com.softdesign.devintensive.data.managers;

import android.content.Context;

import com.softdesign.devintensive.data.network.PicassoCache;
import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDao;
import com.softdesign.devintensive.data.storage.models.UserOrder;
import com.softdesign.devintensive.data.storage.models.UserOrderDao;
import com.softdesign.devintensive.utils.DevApplication;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DataManager {
    private static DataManager INSTANCE;
    private PreferencesManager mPreferencesManager;
    private RestService mRestService;//rest service
    private Context mContext;
    private Picasso mPicasso;

    private DaoSession mDaoSession;


    private DataManager() {
        mContext = DevApplication.getContext();
        mPreferencesManager = new PreferencesManager();
        mRestService = ServiceGenerator.createService(RestService.class);
        mPicasso = new PicassoCache(mContext).getPicassoInstance();
        mDaoSession = DevApplication.getDaoSession();


    }

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    /**
     * rest api auth request
     * @param userLoginReq
     * @return
     */
    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq) {
        return mRestService.loginUser(userLoginReq);
    }

    /**
     * rest api get image request
     * @param url
     * @return
     */
    public Call<ResponseBody> getImage(String url) {
        return mRestService.getImage(url);
    }

    /**
     * rest api upload photo request
     * @param photoFile
     * @return
     */
    public Call<ResponseBody> uploadPhoto(File photoFile) {
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), photoFile);
        MultipartBody.Part bodyPart =
                MultipartBody.Part.createFormData("photo", photoFile.getName(), requestBody);
        return mRestService.uploadImage(bodyPart);
    }

    /**
     * rest api user list request
     * @return
     */
    public Call<UserListRes> getUserListFromNetwork() {
        return mRestService.getUserList();
    }

    // region database

    public DaoSession getDaoSession() {
        return mDaoSession;
    }


    public List<User> getAllUserListOrderedByRatingFromDb() {
        List<User> userList = new ArrayList<>();

        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .orderDesc(UserDao.Properties.Rating)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }

    public List<User> getUserListSortedByNameFromDb(String query) {

        List<User> userList = new ArrayList<>();
        try {
            userList = mDaoSession.queryBuilder(User.class)
                    .where(UserDao.Properties.Rating.gt(0),
                            UserDao.Properties.SearchName.like("%" + query.toUpperCase() + "%"))
//                    .orderDesc(UserDao.Properties.Rating)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }

    //сохраняем в базе порядковые номера всех пользователей
    public void saveUserOrdersInDb(List<User> userList) {

        try {
            mDaoSession.getUserOrderDao().deleteAll();

            for (int i = 0; i < userList.size(); i++) {
                mDaoSession.getUserOrderDao().insertInTx(new UserOrder(userList.get(i).getRemoteId(), i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<User> getUserOrderedListFromDb() {

        List<User> userList = new ArrayList<>();
        try {
            QueryBuilder<User> queryBuilder = mDaoSession.queryBuilder(User.class);
            queryBuilder.join(UserDao.Properties.RemoteId, UserOrder.class, UserOrderDao.Properties.UserRemoteId);
            queryBuilder.where(UserDao.Properties.Rating.gt(0));
            queryBuilder.orderRaw("USER_ORDER ASC");
            queryBuilder.distinct();
            userList = queryBuilder.list();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }


    // endregion
}
