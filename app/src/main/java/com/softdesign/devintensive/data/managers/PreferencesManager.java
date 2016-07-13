package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevApplication;

import java.util.ArrayList;
import java.util.List;

public class PreferencesManager {
    private SharedPreferences mSharedPreferences;

    public static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_MAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_ABOUT_KEY,
    };

    private static final String[] USER_VALUES = {
            ConstantManager.USER_RATING_VALUE,
            ConstantManager.USER_CODE_LINES_VALUE,
            ConstantManager.USER_PROJECTS_VALUE
    };

    private static final String[] USER_NAMES = {
            ConstantManager.USER_FIRST_NAME,
            ConstantManager.USER_SECOND_NAME,
    };

    public PreferencesManager() {
        mSharedPreferences = DevApplication.getSharedPreferences();
    }

    public void saveUserProfileData(List<String> userFields) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < userFields.size(); i++) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }


    public String getUserName() {

        String firstName=mSharedPreferences.getString(ConstantManager.USER_FIRST_NAME, " ");
        String secondName=mSharedPreferences.getString(ConstantManager.USER_SECOND_NAME, " ");
        return secondName+" "+firstName;
    }



    //to login automatically
    public void saveLogin(String login){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_LOGIN,login);
    }

    public void savePassword(String pas){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PASS,pas);
    }

    public String getlogin() {
        return mSharedPreferences.getString(ConstantManager.USER_LOGIN,"");
    }

    public String getPassword() {
        return mSharedPreferences.getString(ConstantManager.USER_PASS,"");
    }

    public String getEmail() {
        return mSharedPreferences.getString(ConstantManager.USER_MAIL_KEY,"");
    }

    /**
     * save user values (rating etc.) in SharedPreferences
     */
    public void saveUserProfileValues(int[] userValues) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_VALUES.length; i++) {
            editor.putString(USER_VALUES[i], String.valueOf(userValues[i]));
        }

        editor.apply();
    }

    public List<String> loadUserProfileValues() {
        List<String> userFields = new ArrayList<>();
        for (String userFieldKey : USER_VALUES) {
            userFields.add(mSharedPreferences.getString(userFieldKey, ""));
        }
        return userFields;
    }

    /**
     * save user name in SharedPreferences
     */
    public void saveUserName(String[] userNames) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_NAMES.length; i++) {
            editor.putString(USER_NAMES[i], userNames[i]);
        }

        editor.apply();
    }

    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        for (String userFieldKey : USER_FIELDS) {
            userFields.add(mSharedPreferences.getString(userFieldKey, ""));
        }
        return userFields;
    }

    public void savePhotoLocalUri(Uri uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_PHOTO_LOCAL_URI, uri.toString());
            editor.apply();
        }
    }

    public void savePhotoUri(String uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_PHOTO_URI, uri);
            editor.apply();
        }
    }

    public void saveAvatarUri(String uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_AVATAR_URI, uri);
            editor.apply();
        }
    }




    public Uri getPhotoLocalUri() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_LOCAL_URI,
                ""));
    }

    public Uri getPhotoUri() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_URI,
                ""));
    }

    public Uri getAvatarUri() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_AVATAR_URI,
                ""));
    }

    /**
     * Save auth token
     *
     * @param authToken токен
     */
    public void saveAuthToken(String authToken) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.AUTH_TOKEN_KEY, authToken);
        editor.apply();
    }

    /**
     * fetch auth token
     *
     * @return
     */
    public String getAuthToken() {
        return mSharedPreferences.getString(ConstantManager.AUTH_TOKEN_KEY, "null");
    }

    /**
     * save userId
     *
     * @param userId
     */
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_ID_KEY, userId);
        editor.apply();
    }

    /**
     * fetch userId
     *
     * @return
     */
    public String getUserId() {
        return mSharedPreferences.getString(ConstantManager.USER_ID_KEY, "null");
    }
}
