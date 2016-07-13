package com.softdesign.devintensive.data.managers;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DataManager {
    private static DataManager INSTANCE;
    private PreferencesManager mPreferencesManager;
    private RestService mRestService;//rest service

    private DataManager() {
        mPreferencesManager = new PreferencesManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
    }

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();
        }
        return INSTANCE;
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
}
