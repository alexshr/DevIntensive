package com.softdesign.devintensive.ui.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Фрагмент для ввода логина и пароля (больше ничего не делает)
 */
public class LoginFragment extends DialogFragment {

    private OnSubmitListener mOnSubmitListener;
    private PreferencesManager mPreferencesManager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.login)
    EditText mLogin;

    @BindView(R.id.password)
    EditText mPassword;


    private Unbinder unbinder;




    @OnClick({R.id.remember, R.id.auth_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auth_button:
                PreferencesManager prefManager = DataManager.getInstance().getPreferencesManager();
                prefManager.saveLogin(mLogin.getText().toString().trim());
                prefManager.savePassword(mPassword.getText().toString().trim());
                mOnSubmitListener.onSubmit();
                break;
            case R.id.remember:
                Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
                startActivity(rememberIntent);

        }
    }


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */

    public static LoginFragment newInstance(String param1, String param2) {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //fullscreen
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fragment_fullscreen);
        mPreferencesManager = DataManager.getInstance().getPreferencesManager();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOnSubmitListener = (OnSubmitListener) context;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * показать сообщение об ошибке
     */
    public void showError(String mes) {

        Utils.showErrorOnSnackBar(mCoordinatorLayout,mes);

    }


    public interface OnSubmitListener {
        void onSubmit();
    }


}
