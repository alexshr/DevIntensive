package com.softdesign.devintensive.ui.activities;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.RoundedAvatarDrawable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static String LOG_TAG = ConstantManager.LOG_TAG;

    private ImageView mCallImg;
    private LinearLayout mUserRatingLayout;
    private AppBarLayout mAppBarLayout;
    private ImageView mToolbarImageView;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mNavigationDrawer;
    private FloatingActionButton mFab;
    private EditText mUserPhone, mUserMail, mUserGit, mUserVk, mUserAboutMe;
    private List<EditText> mUserInfoViews;
    private int mCurrentEditMode;
    private DataManager mDataManager;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        //TODO replace this workaround (or share to sdk 19)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance();
        mCallImg = (ImageView) findViewById(R.id.call_img);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
        mToolbarImageView=(ImageView) findViewById(R.id.toolbar_image_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mUserRatingLayout = (LinearLayout) findViewById(R.id.user_ratings);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange=-1;
            int maxPadding=mUserRatingLayout.getResources().getDimensionPixelSize(R.dimen.spacing_medium_28);
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                int pos=scrollRange+verticalOffset;

                int padding=(pos>=maxPadding?maxPadding:pos);
                mUserRatingLayout.setPadding(0,padding,0,padding);

                Log.d(LOG_TAG, "AppBarLayout scrollRange="+mAppBarLayout.getTotalScrollRange()+" verticalOffset=" + verticalOffset+" padding="+padding);
            }
        });



        mNavigationDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mUserPhone = (EditText) findViewById(R.id.phone_edit);
        mUserMail = (EditText) findViewById(R.id.email_edit);
        mUserGit = (EditText) findViewById(R.id.repo_edit);
        mUserVk = (EditText) findViewById(R.id.vk_profile_edit);
        mUserAboutMe = (EditText) findViewById(R.id.about_edit);

        mUserInfoViews = new ArrayList<>();
        mUserInfoViews.add(mUserPhone);
        mUserInfoViews.add(mUserMail);
        mUserInfoViews.add(mUserGit);
        mUserInfoViews.add(mUserVk);
        mUserInfoViews.add(mUserAboutMe);


        mCallImg.setOnClickListener(this);
        mFab.setOnClickListener(this);
        setupToolbar();
        setupDrawer();


        if (savedInstanceState != null) {
            // recreating activity
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY, 0);
            loadUserInfoValue();
        } else {
            // first starting of the activity
            saveUserInfoValue();
        }
        changeEditorMode(mCurrentEditMode);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_img:
                showProgress();
                break;
            case R.id.fab:
                if (mCurrentEditMode == 0) {
                    mCurrentEditMode = 1;
                } else {
                    mCurrentEditMode = 0;
                }
                changeEditorMode(mCurrentEditMode);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
        saveUserInfoValue();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                showSnackBar(item.getTitle().toString());
                item.setChecked(true);
                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
        // установка круглого аватара
        ImageView userAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user_avatar);
        Bitmap avatar = BitmapFactory.decodeResource(getResources(), R.drawable.user_avatar);
        RoundedAvatarDrawable roundedAvatar = new RoundedAvatarDrawable(avatar);
        userAvatar.setImageDrawable(roundedAvatar);

    }

    /**
     * переключает режим редактирования
     *
     * @param mode 1 - edit,  0 - view
     */
    private void changeEditorMode(int mode) {
        if (mode == 1) {
            mFab.setImageResource(R.drawable.ic_check_black_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setFocusable(true);
                userValue.setEnabled(true);
                userValue.setFocusableInTouchMode(true);
            }
        } else {
            mFab.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            for (EditText userValue : mUserInfoViews) {
                userValue.setFocusable(false);
                userValue.setEnabled(false);
                userValue.setFocusableInTouchMode(false);
            }
        }

    }

    private void loadUserInfoValue() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).setText(userData.get(i));
        }
    }

    private void saveUserInfoValue() {
        List<String> userData = new ArrayList<>();
        for (EditText userInfoView : mUserInfoViews) {
            userData.add(userInfoView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

}
