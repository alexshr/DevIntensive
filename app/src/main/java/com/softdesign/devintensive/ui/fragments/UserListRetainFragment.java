package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.softdesign.devintensive.data.storage.models.User;

import java.util.List;

public class UserListRetainFragment extends Fragment {
    private List<User> mUserList;
    private String mNameFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public List<User> getUserList() {
        return mUserList;
    }



    public String getNameFilter() {
        return mNameFilter;
    }

    public void setNameFilter(String nameFilter) {
        mNameFilter = nameFilter;
    }
}
