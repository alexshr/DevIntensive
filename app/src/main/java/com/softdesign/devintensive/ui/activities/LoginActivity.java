package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private DataManager mDataManager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.login)
    EditText mLogin;
    @BindView(R.id.email_layout)
    TextInputLayout mEmailLayout;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.pass_layout)
    TextInputLayout mPassLayout;


    @OnClick({R.id.remember, R.id.auth_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auth_button:
                login();
                break;
            case R.id.remember:
                rememberPassword();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        mDataManager = DataManager.getInstance();
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void login() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {

            Call<UserModelRes> call = mDataManager.loginUser(
                    new UserLoginReq(mLogin.getText().toString(), mPassword.getText().toString()));
            // asynchr call
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        onLoginSuccess(response.body());
                    } else if (response.code() == 404) {
                        showSnackbar("Неверный логин или пароль");
                    } else {
                        showSnackbar(getString(R.string.error_server));
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    // TODO: handle error
                }
            });

        } else {
            showSnackbar(getString(R.string.error_no_network));
        }
    }

    private void onLoginSuccess(UserModelRes userModel) {
        mDataManager.getPreferencesManager().saveAuthToken(userModel.getData().getToken());
        mDataManager.getPreferencesManager().saveUserId(userModel.getData().getUser().getId());
        saveUserValues(userModel);
        saveUserData(userModel);
        saveUserName(userModel);

        saveImagesUrls(userModel);

        Intent intent = new Intent(this, MainActivity.class);
        /*
        intent.putExtra(ConstantManager.USER_PHOTO_URL_KEY,
                userModel.getData().getUser().getPublicInfo().getPhoto());
        intent.putExtra(ConstantManager.USER_AVATAR_URL_KEY,
                userModel.getData().getUser().getPublicInfo().getAvatar());
        */
        startActivity(intent);
    }

    private void saveImagesUrls(UserModelRes userModel) {
        PreferencesManager pm=mDataManager.getPreferencesManager();
        pm.savePhotoUri(userModel.getData().getUser().getPublicInfo().getPhoto());
        pm.saveAvatarUri(userModel.getData().getUser().getPublicInfo().getAvatar());


    }


    /**
     * save user values (rating ect.) from rest response model to Shared Preferences
     */
    private void saveUserValues(UserModelRes userModel) {
        int[] userValues = {
                userModel.getData().getUser().getProfileValues().getRating(),
                userModel.getData().getUser().getProfileValues().getLinesCode(),
                userModel.getData().getUser().getProfileValues().getProjects()
        };

        mDataManager.getPreferencesManager().saveUserProfileValues(userValues);
    }

    /**
     * save user info (phone, email ect.) from rest response model to Shared Preferences
     */
    private void saveUserData(UserModelRes userModel) {
        List<String> userFields = new ArrayList<>();
        userFields.add(userModel.getData().getUser().getContacts().getPhone());
        userFields.add(userModel.getData().getUser().getContacts().getEmail());
        userFields.add(userModel.getData().getUser().getContacts().getVk());
        userFields.add(userModel.getData().getUser().getRepositories().getRepo().get(0).getGit());
        String bio = userModel.getData().getUser().getPublicInfo().getBio();
        userFields.add(bio.isEmpty() ? "" : bio);

        mDataManager.getPreferencesManager().saveUserProfileData(userFields);
    }

    /**
     * save user name from rest response model to Shared Preferences
     */
    private void saveUserName(UserModelRes userModel) {
        String[] userNames = {
                userModel.getData().getUser().getFirstName(),
                userModel.getData().getUser().getSecondName()
        };

        mDataManager.getPreferencesManager().saveUserName(userNames);
    }


    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}