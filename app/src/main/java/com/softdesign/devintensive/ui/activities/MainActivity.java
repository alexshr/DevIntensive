package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.RoundedAvatarDrawable;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_CAMERA_PICTURE;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_GALLERY_PICTURE;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_PERMISSIONS_CAMERA_SETTINGS;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS;
import static com.softdesign.devintensive.utils.Utils.createImageFile;

public class MainActivity extends BaseActivity {

    private static String LOG_TAG = ConstantManager.LOG_TAG;

    @BindDimen(R.dimen.profile_image_size)
    int mProfileImageSize;

    @BindViews({R.id.phone_input, R.id.email_input, R.id.vk_profile_input, R.id.repo_input, R.id.about_input})
    List<TextInputLayout> mUserInfoViews;

    @BindView(R.id.call_img)
    ImageView mCallImg;
    @BindView(R.id.user_ratings)
    LinearLayout mUserRatingLayout;


    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.scroll_container)
    NestedScrollView mScrollContainer;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.phone_layout)
    LinearLayout mPhoneLayout;
    /*
        @BindView(R.id.phone_edit)
        TextInputEditText mUserPhone;
        @BindView(R.id.email_edit)
        TextInputEditText mUserMail;
        @BindView(R.id.repo_edit)
        TextInputEditText mUserGithub;
        @BindView(R.id.vk_profile_edit)
        TextInputEditText mUserVk;
        @BindView(R.id.about_edit)
        TextInputEditText mUserAboutMe;
    */
    @BindView(R.id.phone_input)
    TextInputLayout mUserPhone;
    @BindView(R.id.email_input)
    TextInputLayout mUserMail;
    @BindView(R.id.repo_input)
    TextInputLayout mUserGithub;
    @BindView(R.id.vk_profile_input)
    TextInputLayout mUserVk;
    @BindView(R.id.about_input)
    TextInputLayout mUserAboutMe;

    //@BindView(R.id.placeholder_profilePhoto_img)
    //ImageView mPlaceholderPhotoImg;
    @BindView(R.id.placeholder_profilePhoto)
    ViewGroup mPlaceholderPhoto;

    @BindView(R.id.user_photo_img)
    ImageView mUserPhotoImg;

    private boolean mIsEditMode;

    private DataManager mDataManager;
    private Handler mUiHandler = new Handler();
    private File mPhotoFile = null;
    private Uri mUri_SelectedImage = null;

   /*
    //подсказка при вводе
    @OnFocusChange({R.id.phone_edit, R.id.vk_profile_edit, R.id.repo_edit})
    public void onFocusChange(View v, boolean hasFocus) {

        switch ((v.getId())) {
            case R.id.phone_edit:
                if (hasFocus) {
                    mUserPhone.setErrorEnabled(true);
                    mUserPhone.setError(getString(R.string.hint_input_phone));
                    mUserPhone.invalidate();
                } else {
                    mUserPhone.setErrorEnabled(false);
                }
                break;
            case R.id.email_edit:
                if (hasFocus) {
                    mUserMail.setErrorEnabled(true);
                    mUserMail.setError(getString(R.string.hint_input_mail));
                } else {
                    mUserMail.setErrorEnabled(false);
                }
                break;
            case R.id.vk_profile_edit:
                if (hasFocus) {
                    mUserVk.setErrorEnabled(true);
                    mUserVk.setError(getString(R.string.hint_input_vk));
                } else {
                    mUserVk.setErrorEnabled(false);
                }
                break;
            case R.id.repo_edit:
                if (hasFocus) {
                    mUserGithub.setErrorEnabled(true);
                    mUserGithub.setError(getString(R.string.hint_input_github));
                } else {
                    mUserGithub.setErrorEnabled(false);
                }
        }
    }
*/
    @OnClick({R.id.call_img, R.id.fab, R.id.placeholder_profilePhoto, R.id.email_img, R.id.vk_img, R.id.repo_img})
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fab:
                if (mIsEditMode) {
                    hideKeyboard();
                    if (validateInput()) {
                        saveUserInfo();
                        mIsEditMode = !mIsEditMode;
                        prepareViewsForMode();
                        mAppBarLayout.setExpanded(false);
                    }
                } else {
                    mIsEditMode = !mIsEditMode;
                    prepareViewsForMode();
                    validateInput();
                }

                break;
            case R.id.placeholder_profilePhoto:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.call_img:
                Intent makeCall = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mUserPhone.getEditText().getText().toString(), null));
                startActivity(makeCall);
                break;
            case R.id.email_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mUserMail.getEditText().getText().toString(), null));
                startActivity(sendEmail);
                break;
            case R.id.vk_img:

                Intent openVK = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mUserVk.getEditText().getText()));
                startActivity(openVK);
                break;
            case R.id.repo_img:
                Intent openGitHub = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mUserGithub.getEditText().getText().toString()));
                startActivity(openGitHub);
                break;
        }
    }

    private boolean validateInput() {
        boolean res = true;
        //boolean isFocusRequested = false;
        //mScrollContainer.scrollBy(0,0);
        //phone
        String phone = (mUserPhone.getEditText().getText() + "").trim();
        mUserPhone.getEditText().setText(phone);
        mUserPhone.getEditText().clearFocus();

        //mUserPhone.setErrorEnabled(false);
        if (!phone.matches(ConstantManager.PHONE_PATTERN)) {
            mUserPhone.setErrorEnabled(true);
            mUserPhone.setError(getString(R.string.hint_input_phone));
            res = false;
        }else{
            mUserPhone.setErrorEnabled(false);
        }


        //mail
        String mail = (mUserMail.getEditText().getText() + "").trim();
        mUserMail.getEditText().setText(mail);
        mUserMail.getEditText().clearFocus();
        //mUserMail.setErrorEnabled(false);
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            mUserMail.setErrorEnabled(true);
            mUserMail.setError(getString(R.string.hint_input_mail));
            res = false;

        }else{
            mUserMail.setErrorEnabled(false);
        }


        //vk

        //mUserVk.setErrorEnabled(false);
        boolean isError = false;
        String vk = (mUserVk.getEditText().getText() + "").trim();
        int pos = vk.indexOf("vk.com");
        if (pos > -1) {
            if (pos > 0) {
                vk = vk.substring(pos);
            }
            if (!vk.matches(ConstantManager.VK_PATTERN)) {
                isError = true;
            }
        } else {
            isError = true;
        }

        if (isError) {
            mUserVk.setErrorEnabled(true);
            mUserVk.setError(getString(R.string.hint_input_vk));

            res = false;

        }else{
            mUserVk.setErrorEnabled(false);
        }


        mUserVk.getEditText().setText(vk);
        mUserVk.getEditText().clearFocus();

        //github
        //mUserGithub.setErrorEnabled(false);
        isError = false;
        String github = (mUserGithub.getEditText().getText() + "").trim();
        pos = github.indexOf(ConstantManager.GITHUB_BASE);
        if (pos > -1) {
            if (pos > 0) {
                github = github.substring(pos);
            }
            if (!github.matches(ConstantManager.GITHUB_PATTERN)) {
                isError = true;
            }
        } else {
            isError = true;
        }
        if (isError) {
            mUserGithub.setErrorEnabled(true);
            mUserGithub.setError(getString(R.string.hint_input_github));
            res = false;

        }else{
            mUserGithub.setErrorEnabled(false);
        }

        mUserGithub.getEditText().setText(github);
        mUserGithub.getEditText().clearFocus();

        /*
        if (!res) {
            mAppBarLayout.setExpanded(false);
        }
        */

        return res;
    }


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
        ButterKnife.bind(this);


        mUserPhone.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        mDataManager = DataManager.getInstance();


        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;
            int maxPadding = mUserRatingLayout.getResources().getDimensionPixelSize(R.dimen.spacing_medium_28);

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                int pos = scrollRange + verticalOffset;

                int padding = (pos >= maxPadding ? maxPadding : pos);
                mUserRatingLayout.setPadding(0, padding, 0, padding);

                Log.d(LOG_TAG, "AppBarLayout scrollRange=" + mAppBarLayout.getTotalScrollRange() + " verticalOffset=" + verticalOffset + " padding=" + padding);
            }
        });

        setupToolbar();
        setupDrawer();


        if (savedInstanceState != null) {
            // recreating activity
            mIsEditMode = savedInstanceState.getBoolean(ConstantManager.EDIT_MODE_KEY, false);
            //loadUserInfoValue();
        } else {
            // first starting of the activity
            loadUserInfoValue();
        }
        placeProfilePicture(mDataManager.getPreferencesManager().loadUserPhoto());
        prepareViewsForMode();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectedItems = getResources().getStringArray(R.array.profile_placeHolder_loadPhotoDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.change_profile_photo));
                builder.setItems(selectedItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int chosenItem) {
                        switch (chosenItem) {
                            case 0:
                                loadPhotoFromCamera();
                                break;
                            case 1:
                                loadPhotoFromGallery();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ConstantManager.EDIT_MODE_KEY, mIsEditMode);
        //saveUserInfo();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mUri_SelectedImage = data.getData();
                    Log.d(LOG_TAG, "onActivityResult: 1" + mUri_SelectedImage.toString());
                    placeProfilePicture(mUri_SelectedImage);
                    mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
                }
                break;
            case REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mUri_SelectedImage = Uri.fromFile(mPhotoFile);
                    Log.d(LOG_TAG, "onActivityResult: 2" + mUri_SelectedImage.toString());
                    placeProfilePicture(mUri_SelectedImage);
                    mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
                }
                break;
            case REQUEST_PERMISSIONS_CAMERA_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ConstantManager.REQUEST_PERMISSIONS_CAMERA:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }


    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPhotoFile = createImageFile(this);
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ConstantManager.REQUEST_PERMISSIONS_CAMERA);
            Snackbar.make(mCoordinatorLayout, R.string.error_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSetting(REQUEST_PERMISSIONS_CAMERA_SETTINGS);
                        }
                    }).show();
        }
    }

    private void loadPhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.hint_choosePhoto)), REQUEST_GALLERY_PICTURE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD);
            Snackbar.make(mCoordinatorLayout, R.string.error_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSetting(REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS);
                        }
                    }).show();
        }
    }

    private void openApplicationSetting(int flag) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, flag);
    }

    private void placeProfilePicture(Uri selectedImage) {
        Picasso.with(this)
                .load(selectedImage)
                .resize(mProfileImageSize, mProfileImageSize)
                .centerInside()
                .placeholder(R.drawable.user_bg)
                .into(mUserPhotoImg);
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
                switch(item.getItemId()){
                    case R.id.logout:
                        logout();
                        break;
                    default:
                        showSnackBar(item.getTitle().toString());
                        item.setChecked(true);
                }

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
     * готовит views под режим (редактирования или просмотра)
     */
    private void prepareViewsForMode() {

        if (mIsEditMode) {
            mFab.setImageResource(R.drawable.ic_check_black_24dp);
            mPlaceholderPhoto.setVisibility(View.VISIBLE);
            for (TextInputLayout userField : mUserInfoViews) {
                userField.getEditText().setFocusable(true);
                userField.getEditText().setEnabled(true);
                userField.getEditText().setFocusableInTouchMode(true);
            }
            mUserPhone.requestFocus();
        } else {
            mFab.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            mPlaceholderPhoto.setVisibility(View.GONE);
            for (TextInputLayout userField : mUserInfoViews) {
                userField.setFocusable(false);
                userField.setEnabled(false);
                userField.setFocusableInTouchMode(false);

            }
        }
    }

    private void loadUserInfoValue() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfoViews.get(i).getEditText().setText(userData.get(i));
        }
    }

    private void saveUserInfo() {
        List<String> userData = new ArrayList<>();
        for (TextInputLayout userInfoView : mUserInfoViews) {
            userData.add(userInfoView.getEditText().getText().toString());
            //userInfoView.getEditText().setEnabled(false);
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);

    }

    private void logout() {
        startActivity(new Intent(this, LoginActivity.class));
    }


}
