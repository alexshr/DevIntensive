package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.softdesign.devintensive.data.network.PicassoCache;
import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserLikesRes;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserOrder;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevApplication;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.InternalQueryDaoAccess;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DataManager {

    private static String LOG_TAG = ConstantManager.LOG_TAG + "_DataManager";
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
     *
     * @param userLoginReq
     * @return
     */
    public Call<UserModelRes> loginUser(UserLoginReq userLoginReq) {
        return mRestService.loginUser(userLoginReq);
    }

    /**
     * rest api get image request
     *
     * @param url
     * @return
     */
    public Call<ResponseBody> getImage(String url) {
        return mRestService.getImage(url);
    }

    /**
     * rest api upload photo request
     *
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
     *
     * @return
     */
    public Call<UserListRes> getUserListFromNetwork() {
        return mRestService.getUserList();
    }


    /**
     * rest api like request
     *
     * @return
     */
    public Call<UserLikesRes> like(String recipientUserId,boolean isChecked) {
        if(isChecked){
            return mRestService.like(recipientUserId);
        }else{
            return mRestService.unLike(recipientUserId);
        }
    }



    // region database

    public DaoSession getDaoSession() {
        return mDaoSession;
    }


   //сохраняем в отд таблице порядок карточек
    public void saveUserListOrderInDb(List<User> userList) {

        try {
            mDaoSession.getUserOrderDao().deleteAll();

            for (int i = 0; i < userList.size(); i++) {
                mDaoSession.getUserOrderDao().insertInTx(new UserOrder(userList.get(i).getRemoteId(), i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * список пользователей, работает и при обновлении с сервера - новые записи идут вниз
     * Этот запрос - единственный на все случаи вывода списка!!
     * Захотим вернуть стандартную сортировку - просто удаляем таблицу user_order
     * <p>
     * Пришлось извращаться, т.к. greendao не умеет left join,
     * а обычный join приведет к тому, что при обновлениии все новые записи будут невидимы
     * <p>
     * благодаря крутому выражению в сортировке: USER_ORDER IS NULL ASC
     * новые записи идут в конец списка
     *
     * @param nameFilter
     * @return
     */
    public List<User> getUserListFromDb(String nameFilter) {


        String[] params = new String[]{};
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(" SELECT  T._id,T.REMOTE_ID,T.PHOTO,T.FULL_NAME,T.SEARCH_NAME,T.RATING,T.CODE_LINES,T.PROJECTS,T.BIO");
            sb.append(" FROM USERS T");
            sb.append(" LEFT JOIN USER_ORDER J1 ON T.REMOTE_ID=J1.USER_REMOTE_ID");
            sb.append(" WHERE T.RATING>0");
            if (nameFilter != null && nameFilter.length() > 0) {
                sb.append(" AND T.SEARCH_NAME LIKE ? ");
                params = new String[]{"%"+nameFilter+"%"};
            }
            sb.append(" ORDER BY USER_ORDER IS NULL ASC,USER_ORDER ASC, RATING DESC");

            //Log.d(LOG_TAG, "getUserListFromDb before daoAccess.loadAllAndCloseCursor");

            AbstractDao dao = getDaoSession().getDao(User.class);
            Cursor cursor = dao.getDatabase().rawQuery(sb.toString(), params);
            InternalQueryDaoAccess daoAccess = new InternalQueryDaoAccess<User>(dao);
            //Log.d(LOG_TAG,"getUserListFromDb() filter="+(nameFilter==null?"null":nameFilter));



            List<User> users= daoAccess.loadAllAndCloseCursor(cursor);
            /*
            for(User user:users){
                Log.d(LOG_TAG,"user from db: "+user);
            }
            */
            return users;

        } catch (Exception e) {
            Log.e(LOG_TAG, "", e);
            return null;
        }

    }
}