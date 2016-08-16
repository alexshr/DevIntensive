package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.tasks.LoadUserListFromDbTask;
import com.softdesign.devintensive.data.storage.tasks.SaveUserOrdersInDbTask;
import com.softdesign.devintensive.services.DownloadDataService;
import com.softdesign.devintensive.ui.adapters.UserListAdapter;
import com.softdesign.devintensive.ui.views.ToggleImageButton;
import com.softdesign.devintensive.utils.BorderedCircleTransform;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends BaseManagerActivity {

    private static final String LOG_TAG = ConstantManager.LOG_TAG + "_UserListActivity";

    //private static final String TAG_RETAIN_FRAGMENT = "rf";
    private static final String KEY_FILTER = "KEY_FILTER";
    private static final String KEY_SEARCH_EXPANDED = "KEY_SEARCH_EXPANDED";


    private DataManager mDataManager;
    private UserListAdapter mUserListAdapter;


    @BindView(R.id.main_coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;

    @BindView(R.id.splash)
    LinearLayout mSplash;

    private List<User> mUserList;

    private String mFilter;

    //тут данные больше не храню!!!!
    //беру прямо из базы
    //потому что она может, например, актуализироваться сервером по расписанию
    //private UserListRetainFragment mRetainFragment;

    private final ChronosConnector mChronosConnector = new ChronosConnector();

    private int currTask;
    private PreferencesManager mPrefManager;

    private boolean mIsUserOrderChanged;

    private boolean mIsSearchExpanded;

    private SearchView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        if (savedInstanceState != null) {
            mFilter = savedInstanceState.getString(KEY_FILTER);
            mIsSearchExpanded = savedInstanceState.getBoolean(KEY_SEARCH_EXPANDED);
        }

        ButterKnife.bind(this);


        mChronosConnector.onCreate(this, savedInstanceState);

        mDataManager = DataManager.getInstance();
        mPrefManager = mDataManager.getPreferencesManager();

        setupToolBar();
        setupDrawer();

        //layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        //touch helper для swipe перемещения и удаления карточек
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                mIsUserOrderChanged = true;

                getUserList().add(toPos, getUserList().remove(fromPos));
                mUserListAdapter.notifyItemMoved(fromPos, toPos);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                getUserList().remove(position);
                mUserListAdapter.notifyDataSetChanged();
                mIsUserOrderChanged = true;

            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }


    @Override
    public void showSplash() {
        super.showSplash();
        mSplash.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSplash() {
        super.hideSplash();
        mSplash.setVisibility(View.GONE);
    }

    /**
     * лок данные берутся из базы каждый раз
     * хранить их в retain fragment неверно
     * потому что например сервис по расписанию обновит данные,
     * и мы тут об этом узнаем, а в retain fragment - нет
     */
    @Override
    public void showData() {
        Log.d(LOG_TAG, "showData started");

        //получаем полный список с выбранной сортировкой
        //поток запускаем в chronos
        mChronosConnector.runOperation(
                new LoadUserListFromDbTask(mFilter), false);
    }

    @Override
    public void showErrorMes(String mes) {
        hideProgress();
        Utils.showErrorOnSnackBar(mCoordinatorLayout, mes);
    }


    @Override
    public void showInfoMes(String mes) {
        hideProgress();
        Utils.showInfoOnSnackBar(mCoordinatorLayout, mes);
    }


    @Override
    public void downloadData() {
        Log.d(LOG_TAG, "downloadData started");
        super.downloadData();
        if (mPrefManager.getAuthToken().isEmpty()) {
            if (mPrefManager.getLogin().isEmpty()) {
                //отправляемся спрашивать у пользователя логин и пароль
                postEvent(BaseManagerActivity.MES_USER_NOT_AUTHORIZED);
            } else {
                //идем на авторизацию и сразу взятие списка
                DownloadDataService.startActionFull(this, mPrefManager.getLogin(), mPrefManager.getPassword());
            }
        } else {
            //есть токен, а значит шансы получить список без авторизации
            DownloadDataService.startActionUserList(this);
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mChronosConnector.onSaveInstanceState(outState);
        //search filter
        outState.putString(KEY_FILTER, mFilter);
        //search view state
        outState.putBoolean(KEY_SEARCH_EXPANDED, !mSearchView.isIconified());


    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "onStop started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChronosConnector.onResume();
    }

    @Override
    protected void onPause() {
        mChronosConnector.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIsUserOrderChanged) {
            mPrefManager.saveIsUserListOrderChanged(true);
            mChronosConnector.runOperation(
                    new SaveUserOrdersInDbTask(getUserList()), false);
        }

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem searchMenuItem = menu.findItem(R.id.search);
        //searchMenuItem.collapseActionView();

        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchView.setQueryHint(getString(R.string.enter_user_name));

        if (mIsSearchExpanded) {
            searchMenuItem.expandActionView();
        }
        if (mFilter != null && mFilter.length() > 0) {
            mSearchView.setQuery(mFilter, false);
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                //ввели новый символ - отменяем поиск и запускаем актуальный
                mFilter = newText.trim();
                if (mChronosConnector.isOperationRunning(currTask)) {
                    mChronosConnector.cancelOperation(currTask, true);
                }
                currTask = mChronosConnector.runOperation(new LoadUserListFromDbTask(mFilter), false);
                return true;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }


    private void setupToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.team);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mNavigationDrawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.refresh:
                downloadData();
                return true;
            case R.id.search:
                mIsSearchExpanded = item.isActionViewExpanded();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupDrawer() {

        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:

                        Intent intent = new Intent(UserListActivity.this, HostProfileActivity.class);
                        startActivity(intent);
                        finish();

                        break;
                    case R.id.team:

                        break;
                    case R.id.logout:
                        logout();


                }
                item.setChecked(true);
                mNavigationDrawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        // установка круглого аватара
        ImageView userAvatarImg = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.avatar);
        Uri avatarUri = mDataManager.getPreferencesManager().getAvatarUri();
        Picasso.with(this)
                .load(avatarUri)
                //.resize(mProfileImageSize, mProfileImageSize)
                //.centerInside()
                .placeholder(R.drawable.user_avatar)
                .transform(new BorderedCircleTransform())
                .into(userAvatarImg);

        //show user name and email
        TextView userNameView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_name);
        String userName = mDataManager.getPreferencesManager().getUserName();
        userNameView.setText(userName);

        TextView userEmailView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_email);
        String email = mDataManager.getPreferencesManager().getEmail();
        userEmailView.setText(email);
    }

    private void showUserList(List<User> users) {
        Log.d(LOG_TAG, "showUserList count=" + users.size());

        if (mUserListAdapter == null) {
            setUserList(users);
            mUserListAdapter = new UserListAdapter(users, new UserListAdapter.UserViewHolder.UserItemClickListener() {
                @Override
                public void onUserItemClick(int adapterPosition, View view) {
                    User user = mUserListAdapter.getUser(adapterPosition);

                    switch (view.getId()) {
                        case R.id.more_info_btn:

                            Intent intent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                            intent.putExtra(ConstantManager.USER_DTO_KEY, new UserDTO(user));

                            startActivity(intent);
                            break;
                        case R.id.likes_by_btn: {
                            ToggleImageButton btn = (ToggleImageButton) findViewById(R.id.likes_by_btn);
                            //btn.setChecked(!btn.isChecked());
                            DownloadDataService.startActionLike(UserListActivity.this, user.getRemoteId(), btn.isChecked());
                        }

                    }
                }
            });

            mRecyclerView.setAdapter(mUserListAdapter);
        } else {
            mUserListAdapter.swap(users);
        }
        hideProgress();
    }

    @Override
    public void checkCurrentDrawerMenuItem() {
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    public List<User> getUserList() {
        return mUserList;
    }

    public void setUserList(List<User> userList) {
        mUserList = userList;
    }

    /**
     * chronos
     *
     * @param result
     */
    public void onOperationFinished(final LoadUserListFromDbTask.Result result) {

        Log.d(LOG_TAG, "onOperationFinished result result.isSuccessful() " + result.isSuccessful() + " count=" + result.getOutput().size());
        if (result.isSuccessful()) {
            showUserList(result.getOutput());
        } else {
            showErrorMes(result.getErrorMessage());
        }
    }

    public void onOperationFinished(final SaveUserOrdersInDbTask.Result result) {
        Log.d(LOG_TAG, "SaveUserOrdersInDbTask result result.isSuccessful() " + result.isSuccessful() + " result data isn not interesting");
        if (!result.isSuccessful()) {

            Log.e(LOG_TAG, "", result.getException());
        }
    }


}
