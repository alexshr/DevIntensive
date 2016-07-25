package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.services.DownloadDataService;
import com.softdesign.devintensive.ui.views.ValidatedTextInputLayout;
import com.softdesign.devintensive.utils.BorderedCircleTransform;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_CAMERA_PICTURE;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_GALLERY_PICTURE;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_PERMISSIONS_CAMERA_SETTINGS;
import static com.softdesign.devintensive.utils.ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS;
import static com.softdesign.devintensive.utils.Utils.createImageFile;

/**
 * окно профиля залогинившегося пользователя
 * вся логика работы полей ввода, включая валидацию, подсказки и пр перенесены в custom view
 */
public class HostProfileActivity extends BaseManagerActivity {


    private static String LOG_TAG = ConstantManager.LOG_TAG;

    //image size
    @BindDimen(R.dimen.profile_image_size)
    int mProfileImageSize;

    @BindView(R.id.placeholder_layout)
    ViewGroup mPlaceholderPhoto;

    //rating layout
    @BindView(R.id.user_ratings)
    LinearLayout mUserRatingLayout;

    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;

    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    //image for foto
    @BindView(R.id.photo_img)
    ImageView mUserPhotoImg;

    //list of input layouts
    @BindViews({R.id.phone_layout, R.id.email_layout, R.id.vk_layout, R.id.github_layout, R.id.about_layout})
    List<ValidatedTextInputLayout> mEditLayoutList;

    //list of user values
    @BindViews({R.id.rating, R.id.lines, R.id.projects})
    List<TextView> mValueList;

    //edit fields layouts
    @BindView(R.id.phone_layout)
    ValidatedTextInputLayout mUserPhoneLayout;

    @BindView(R.id.email_layout)
    ValidatedTextInputLayout mUserMailLayout;

    @BindView(R.id.github_layout)
    ValidatedTextInputLayout mUserGithub;

    @BindView(R.id.vk_layout)
    ValidatedTextInputLayout mUserVkLayout;

    @BindView(R.id.about_layout)
    ValidatedTextInputLayout mUserAboutLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;


    // edit mode
    private boolean mIsEditMode;


    private DataManager mDataManager = DataManager.getInstance();
    private PreferencesManager mPrefManager = mDataManager.getPreferencesManager();

    //file from camera
    private File mPhotoFile = null;

    private Uri mPhotoUri = null;


    @OnClick({R.id.call_img, R.id.fab, R.id.placeholder_layout, R.id.email_img, R.id.vk_img, R.id.repo_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:

                hideKeyboard();
                if (mIsEditMode) {
                    ValidatedTextInputLayout firstErrorLayout = getFirstErrorInput();
                    if (firstErrorLayout == null) {
                        //all is valid
                        saveUserInfo();
                        mIsEditMode = !mIsEditMode;
                        //mAppBarLayout.setExpanded(false);
                    }
                } else {
                    mIsEditMode = !mIsEditMode;
                }
                prepareViewsForMode();

                break;
            case R.id.placeholder_layout:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.call_img:
                Intent makeCall = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mUserPhoneLayout.getText(), null));
                startActivity(makeCall);
                break;
            case R.id.email_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mUserPhoneLayout.getText(), null));
                startActivity(sendEmail);
                break;
            case R.id.vk_img:
                Intent openVK = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mUserPhoneLayout.getText()));
                startActivity(openVK);
                break;
            case R.id.repo_img:
                Intent openGitHub = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mUserPhoneLayout.getText()));
                startActivity(openGitHub);
                break;
        }
    }

    /**
     * determine first error field
     *
     * @return first error input or null if all fields data is valid
     */
    private ValidatedTextInputLayout getFirstErrorInput() {
        ValidatedTextInputLayout errorLayout = null;
        boolean res = true;
        for (ValidatedTextInputLayout editLayout : mEditLayoutList) {
            if (!editLayout.isTextValid()) {
                return editLayout;
            }
        }
        return null;
    }

    /**
     * init focus in edit mode
     */
    private void requestFocus() {
        if (mIsEditMode) {
            ValidatedTextInputLayout firstErrorLayout = getFirstErrorInput();
            if (firstErrorLayout == null) {
                mEditLayoutList.get(0).getEditText().requestFocus();
            } else {
                firstErrorLayout.requestFocus();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_host_profile);


        ButterKnife.bind(this);


        setupToolbar();

        setupDrawer();


        if (savedInstanceState != null) {
            // recreating activity
            mIsEditMode = savedInstanceState.getBoolean(ConstantManager.EDIT_MODE_KEY, false);
            //loadUserInfoValue();
        }
    }

    @Override
    public void showData() {
        Log.d(LOG_TAG, "showData started");
        super.showData();
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mEditLayoutList.get(i).setText(userData.get(i));
        }

        userData = mDataManager.getPreferencesManager().loadUserProfileValues();
        for (int i = 0; i < userData.size(); i++) {
            mValueList.get(i).setText(userData.get(i));
        }
        prepareViewsForMode();

        // photo from server
        placeProfilePicture(mDataManager.getPreferencesManager().getPhotoUri());

        postEvent(MES_SHOW_FINISHED);
    }

    @Override
    public void showErrorMes(String mes) {
        Utils.showErrorOnSnackBar(mCoordinatorLayout, mes);
    }

    @Override
    public void showInfoMes(String mes) {
        Utils.showInfoOnSnackBar(mCoordinatorLayout, mes);
    }


    @Override
    public void downloadData() {
        Log.d(LOG_TAG, "downloadData started");
        super.downloadData();
        if (mPrefManager.getLogin().isEmpty()) {
            //отправляемся спрашивать у пользователя логин и пароль
            postEvent(BaseManagerActivity.MES_USER_NOT_AUTHORIZED);
        } else {
            //идем за профайлом и токеном
            DownloadDataService.startActionTokenAndProfile(this, mPrefManager.getLogin(), mPrefManager.getPassword());
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectedItems = getResources().getStringArray(R.array.photo_dialog_choices);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.host_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            downloadData();
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
                    mPhotoUri = data.getData();
                    mDataManager.getPreferencesManager().savePhotoLocalUri(mPhotoUri);
                    Log.d(LOG_TAG, "onActivityResult: 1" + mPhotoUri.toString());
                    placeProfilePicture(mPhotoUri);
                    uploadImageFile(new File(getPath(mPhotoUri)));
                }
                return;
            case REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mPhotoUri = Uri.fromFile(mPhotoFile);
                    mDataManager.getPreferencesManager().savePhotoLocalUri(mPhotoUri);
                    Log.d(LOG_TAG, "onActivityResult: 2" + mPhotoUri.toString());
                    placeProfilePicture(mPhotoUri);
                    uploadImageFile(new File(mPhotoUri.getPath()));

                }
                return;
            case REQUEST_PERMISSIONS_CAMERA_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                return;
            case REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                return;

        }

    }

    /**
     * get file path for galery uri
     *
     * @param uri URI файла
     * @return путь к файлу
     */
    //@Nullable
    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
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
                .centerCrop()
                .placeholder(R.drawable.user_bg)
                .into(mUserPhotoImg);

    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbar.setTitle(mDataManager.getPreferencesManager().getUserName());

    }

    private void setupDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.team:
                        Intent intent = new Intent(HostProfileActivity.this, UserListActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.logout:
                        logout();
                        break;
                }
                item.setChecked(true);
                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        // установка круглого аватара
        ImageView userAvatarImg = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
        Uri avatarUri = mDataManager.getPreferencesManager().getAvatarUri();
        Picasso.with(this)
                .load(avatarUri)
                //.resize(mProfileImageSize, mProfileImageSize)
                //.centerInside()
                .placeholder(R.drawable.user_avatar)
                .transform(new BorderedCircleTransform())
                .into(userAvatarImg);

        //show user name and email
        TextView userNameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        String userName = mDataManager.getPreferencesManager().getUserName();
        userNameView.setText(userName);

        TextView userEmailView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);
        String email = mDataManager.getPreferencesManager().getEmail();
        userEmailView.setText(email);


    }


    /**
     * upload photo to server
     *
     * @param photoFile - file with photo
     */
    private void uploadImageFile(File photoFile) {
        Call<ResponseBody> call = mDataManager.uploadPhoto(photoFile);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("TAG", "Upload success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(mCoordinatorLayout, R.string.error_upload_photo, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * готовит views под режим (редактирования или просмотра)
     */
    private void prepareViewsForMode() {

        if (mIsEditMode) {
            mFab.setImageResource(R.drawable.ic_check_black_24dp);
            mPlaceholderPhoto.setVisibility(View.VISIBLE);
        } else {
            mFab.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            mPlaceholderPhoto.setVisibility(View.GONE);
        }
        for (ValidatedTextInputLayout userField : mEditLayoutList) {
            userField.setInEditMode(mIsEditMode);
        }

        requestFocus();
    }


    private void saveUserInfo() {
        List<String> userData = new ArrayList<>();
        for (ValidatedTextInputLayout userInfoView : mEditLayoutList) {
            userData.add(userInfoView.getText());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);

    }


}
