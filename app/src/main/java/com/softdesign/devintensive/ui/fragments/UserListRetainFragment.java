package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Фрагмент больше не нужен (оставил в память о задании)
 * лок. данные теперь хранятся в базе
 */

public class UserListRetainFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

}
