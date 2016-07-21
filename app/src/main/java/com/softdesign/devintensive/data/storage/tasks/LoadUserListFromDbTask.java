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
    public static final String SORTED_BY_USER_LIST = "SORTED_BY_USER_LIST";
    public static final String SEARCH_LIST = "SEARCH_LIST";

    private String mCrit = "";
    private String mQuery;

    private String LOG_TAG = ConstantManager.LOG_TAG;

    public LoadUserListFromDbTask() {

    }

    public LoadUserListFromDbTask(String userName, String criteria) {
        mQuery = userName;
        mCrit = criteria;
    }

    @Nullable
    @Override
    public List<User> run() {
        final List<User> res;

        Log.e(LOG_TAG, "LoadUserListFromDbTask run crit=" + mCrit);
        if (mCrit.isEmpty()) {
            res = DataManager.getInstance().getAllUserListOrderedByRatingFromDb();
        } else {
            switch (mCrit) {
                case SEARCH_LIST:
                    res = DataManager.getInstance().getUserListSortedByNameFromDb(mQuery);
                    break;

                case SORTED_BY_USER_LIST:
                    res = DataManager.getInstance().getUserOrderedListFromDb();
                    break;

                default:
                    res=null;
                    Log.e(LOG_TAG, "!!!LoadUserListFromDbTask run wrong sort crit");
            }
        }

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
