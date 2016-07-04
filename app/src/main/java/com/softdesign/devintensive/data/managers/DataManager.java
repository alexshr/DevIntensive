package com.softdesign.devintensive.data.managers;

public class DataManager {
    private static DataManager ourInstance;
    private PreferencesManager mPreferencesManager;

    private DataManager() {
        mPreferencesManager = new PreferencesManager();
    }

    public static DataManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new DataManager();
        }
        return ourInstance;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }
}
