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

    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        for (String userFieldKey : USER_FIELDS) {
            userFields.add(mSharedPreferences.getString(userFieldKey,""));
        }
        return userFields;
    }

    public void saveUserPhoto(Uri uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_PROFILE_PHOTO_URI, uri.toString());
            editor.apply();
        }
    }

    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PROFILE_PHOTO_URI,
                ""));
    }
}
