package com.softdesign.devintensive.utils;

public interface ConstantManager {
    String LOG_TAG = "DEV";

    //keys for store in PreferenceManager
    String USER_ID_KEY = "USER_ID_KEY";
    String AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY";

    String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    String USER_PHONE_KEY = "USER_PHONE_KEY";
    String USER_MAIL_KEY = "USER_MAIL_KEY";
    String USER_VK_KEY = "USER_VK_KEY";
    String USER_GIT_KEY = "USER_GIT_KEY";
    String USER_ABOUT_KEY = "USER_ABOUT_KEY";

    String USER_RATING_VALUE = "USER_RATING_VALUE";
    String USER_CODE_LINES_VALUE = "USER_CODE_LINES_VALUE";
    String USER_PROJECTS_VALUE = "USER_PROJECTS_VALUE";

    String USER_FIRST_NAME = "USER_FIRST_NAME";
    String USER_SECOND_NAME = "USER_SECOND_NAME";

    String USER_PHOTO_LOCAL_URI = "USER_PHOTO_LOCAL_URI";

    String USER_PHOTO_URI = "USER_PHOTO_URI";
    String USER_AVATAR_URI = "USER_AVATAR_URI";

    String USER_DTO_KEY ="USER_DTO_KEY" ;
    String USER_LOGIN_KEY = "USER_LOGIN_KEY";

    String USER_PASS_KEY = "USER_PASS_KEY";





    //Dialog constants
    int LOAD_PROFILE_PHOTO = 1;

    //Request permission constants
    int REQUEST_PERMISSIONS_CAMERA = 11;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 12;
    int REQUEST_PERMISSIONS_READ_SDCARD = 13;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 14;

    //activity request codes
    int REQUEST_CAMERA_PICTURE = 1;
    int REQUEST_GALLERY_PICTURE = 2;



    String SORT_CRITERIA = "SORT_CRITERIA";



}
