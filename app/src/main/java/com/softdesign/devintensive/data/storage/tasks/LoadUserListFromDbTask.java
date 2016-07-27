package com.softdesign.devintensive.data.storage.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.redmadrobot.chronos.ChronosOperation;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.List;

public class LoadUserListFromDbTask extends ChronosOperation<List<User>> {
    private String mFilter = "";
    private List<User> res;


    private String LOG_TAG = ConstantManager.LOG_TAG;

    public LoadUserListFromDbTask() {

    }

    public LoadUserListFromDbTask(String userName) {
        mFilter = userName;
    }

    @Nullable
    @Override
    public List<User> run() {

        Log.d(LOG_TAG, "LoadUserListFromDbTask run started filter=" + mFilter);

        res = DataManager.getInstance().getUserListFromDb(mFilter);

        return res;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<User>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<User>> {

    }
}
