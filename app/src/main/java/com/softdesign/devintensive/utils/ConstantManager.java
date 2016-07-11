package com.softdesign.devintensive.utils;

public interface ConstantManager {
    String LOG_TAG = "DEV";

    String VK_BASE = "vk.com";
    String GITHUB_BASE = "github.com";

    //String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    String PHONE_PATTERN ="^[+][0-9]{11,20}$";
    String VK_PATTERN ="^("+VK_BASE+"/)[a-zA-Z0-9-]+$";
    String GITHUB_PATTERN ="^("+GITHUB_BASE+"/)[a-zA-Z0-9-]+$";



    String EDIT_MODE_KEY = "EDIT_MODE_KEY";

    String USER_PHONE_KEY = "USER_PHONE_KEY";
    String USER_MAIL_KEY = "USER_MAIL_KEY";
    String USER_VK_KEY = "USER_VK_KEY";
    String USER_GIT_KEY = "USER_GIT_KEY";
    String USER_ABOUT_KEY = "USER_ABOUT_KEY";

    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";

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

}
