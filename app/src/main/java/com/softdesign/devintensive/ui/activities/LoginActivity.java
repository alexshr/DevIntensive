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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.network.req.UserLoginReq;
import com.softdesign.devintensive.data.network.res.UserModelRes;
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
    private PreferencesManager mPrefManager;

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

    @BindView(R.id.progress)
    ProgressBar mProgressBar;

    @BindView(R.id.auth_layout)
    RelativeLayout mAuthLayout;


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
        mAuthLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mDataManager = DataManager.getInstance();
        mPrefManager = mDataManager.getPreferencesManager();
        if (mPrefManager.getLogin().length() > 0) {
            showProgress();
            login();
        } else {
            showLoginLayout();
        }
    }


    private void showProgress() {
        mAuthLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        /*
        mProgressBar.getIndeterminateDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
*/
    }

    private void showLoginLayout() {
        mAuthLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void rememberPassword() {
        Intent rememberIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://devintensive.softdesign-apps.ru/forgotpass"));
        startActivity(rememberIntent);
    }

    private void login() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {

            String login = mLogin.getText().toString();
            String pass = mPassword.getText().toString();
            if (mPrefManager.getLogin().length() > 0) {
                login = mPrefManager.getLogin();
                pass = mPrefManager.getPassword();
            }

            Call<UserModelRes> call = mDataManager.loginUser(
                    new UserLoginReq(login, pass));
            // asynchr call
            call.enqueue(new Callback<UserModelRes>() {
                @Override
                public void onResponse(Call<UserModelRes> call, Response<UserModelRes> response) {
                    if (response.code() == 200) {
                        onLoginSuccess(response.body());
                    } else if (response.code() == 404) {
                        mPrefManager.removeAuth();
                        showSnackbar("Неверный логин или пароль");
                    } else {
                        mPrefManager.removeAuth();
                        showSnackbar(getString(R.string.error_server));
                    }
                }

                @Override
                public void onFailure(Call<UserModelRes> call, Throwable t) {
                    // TODO: handle error
                    mPrefManager.removeAuth();
                    showLoginLayout();
                }
            });

        } else {
            showSnackbar(getString(R.string.error_no_network));
        }
    }

    private void onLoginSuccess(UserModelRes userModel) {

        mPrefManager.saveUserId(userModel.getData().getUser().getId());
        mPrefManager.saveAuthToken(userModel.getData().getToken());

        if(mPrefManager.getLogin().isEmpty()){
            mPrefManager.saveLogin(mLogin.getText().toString());
            mPrefManager.savePassword(mPassword.getText().toString());
        }

        saveUserValues(userModel);
        saveUserData(userModel);
        saveUserName(userModel);

        saveImagesUrls(userModel);

        Intent intent = new Intent(this, UserListActivity.class);

        startActivity(intent);
        finish();
    }

    private void saveImagesUrls(UserModelRes userModel) {

        mPrefManager.savePhotoUri(userModel.getData().getUser().getPublicInfo().getPhoto());
        mPrefManager.saveAvatarUri(userModel.getData().getUser().getPublicInfo().getAvatar());


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

        mPrefManager.saveUserProfileValues(userValues);
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

        mPrefManager.saveUserProfileData(userFields);
    }

    /**
     * save user name from rest response model to Shared Preferences
     */
    private void saveUserName(UserModelRes userModel) {
        String[] userNames = {
                userModel.getData().getUser().getFirstName(),
                userModel.getData().getUser().getSecondName()
        };

        mPrefManager.saveUserName(userNames);
    }


    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}