package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.softdesign.devintensive.data.network.res.UserListRes;

import java.util.ArrayList;
import java.util.List;

public class UserListRetainFragment extends Fragment {
    private List<UserListRes.UserData> mUsersList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public List<UserListRes.UserData> getUsersList() {
        return mUsersList;
    }

    public void setUsersList(List<UserListRes.UserData> usersList) {
        mUsersList = usersList;
    }
}
