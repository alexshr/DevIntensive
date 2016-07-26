package com.softdesign.devintensive.data.storage.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.ArrayList;
import java.util.List;

/**
 * сохраняем порядок в списке
 */

public class SaveUserOrdersInDbTask extends ChronosOperation<List<User>> {
    public static final String FULL_LIST = "FULL_LIST";
    public static final String FILTERED_LIST = "FILTERED_LIST";
    private List<User> mUserList = new ArrayList<>();


    private String LOG_TAG = ConstantManager.LOG_TAG;

    public SaveUserOrdersInDbTask() {

    }

    public SaveUserOrdersInDbTask(List<User> userList) {
        mUserList = userList;

    }

    @Nullable
    @Override
    public List<User> run() {

        Log.d(LOG_TAG, "SaveUserOrdersInDbTask run started");
        DataManager.getInstance().saveUserListOrderInDb(mUserList);


        return mUserList;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<User>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {

    }
}
