package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.redmadrobot.chronos.ChronosConnector;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.tasks.LoadUserListFromDbTask;
import com.softdesign.devintensive.ui.adapters.UserListAdapter;
import com.softdesign.devintensive.ui.fragments.UserListRetainFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.BorderedCircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends BaseActivity {

    private static final String LOG_TAG = ConstantManager.LOG_TAG + "_UserListActivity";

    private static final String TAG_RETAIN_FRAGMENT = "rf";


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

    @BindView(R.id.progress)
    ProgressBar mProgressBar;


    private UserListRetainFragment mRetainFragment;

    private final ChronosConnector mChronosConnector = new ChronosConnector();
    private List<User> mUsers;
    private int currTask;
    private MenuItem mSearchMenuItem;
    private PreferencesManager mPrefManager;
    private String mSortCriteria;
    private int mCurrTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        mChronosConnector.onCreate(this, savedInstanceState);

        mDataManager = DataManager.getInstance();

        setupToolBar();
        setupDrawer();

        mRetainFragment = (UserListRetainFragment) getSupportFragmentManager().findFragmentByTag(TAG_RETAIN_FRAGMENT);
        if (mRetainFragment == null) {
            mRetainFragment = new UserListRetainFragment();
            getSupportFragmentManager().beginTransaction().add(mRetainFragment, TAG_RETAIN_FRAGMENT).commit();
        }


        mSortCriteria = mDataManager.getPreferencesManager().getSortCriteria();
        if (mRetainFragment.getUserList() == null) {
            //проверяем наличие данных
            //получаем полный список с выбранной сортировкой
            //поток запускаем в chronos
            mCurrTask = mChronosConnector.runOperation(
                    new LoadUserListFromDbTask(null, mSortCriteria), false);

        } else {
            mUsers = mRetainFragment.getUserList();


            showUserList(mRetainFragment.getUserList());
        }


        //layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        //touch helper для swipe перемещения и удаления карточек
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();

                mUsers.add(toPos, mUsers.remove(fromPos));
                mUserListAdapter.notifyItemMoved(fromPos, toPos);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mUsers.remove(position);
                mUserListAdapter.notifyDataSetChanged();
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }


    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        mChronosConnector.onSaveInstanceState(outState);
        mRetainFragment.setUserList(mUsers);
        mDataManager.saveUserOrdersInDb(mUsers);
        if (mSortCriteria == null || mSortCriteria.equals("")) {
            mPrefManager.saveSortCriteria(LoadUserListFromDbTask.SORTED_BY_USER_LIST);
        } else {
            mDataManager.getPreferencesManager().saveSortCriteria(mSortCriteria);
        }
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
        mDataManager.saveUserOrdersInDb(mUsers);
        if (mSortCriteria == null || mSortCriteria.equals("")) {
            mDataManager.getPreferencesManager().saveSortCriteria(LoadUserListFromDbTask.SORTED_BY_USER_LIST);
        } else {
            mDataManager.getPreferencesManager().saveSortCriteria(mSortCriteria);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        mSearchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        searchView.setQueryHint(getString(R.string.enter_user_name));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //ввели новый символ - отменяем поиск и запускаем актуальный
                if (mChronosConnector.isOperationRunning(currTask)) {
                    mChronosConnector.cancelOperation(currTask, true);
                }
                currTask = mChronosConnector.runOperation(
                        new LoadUserListFromDbTask(newText, LoadUserListFromDbTask.SEARCH_LIST), false);


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


    private List<UserListRes.UserData> filter(List<UserListRes.UserData> models, String query) {
        query = query.toLowerCase();

        final List<UserListRes.UserData> filteredModelList = new ArrayList<>();
        for (UserListRes.UserData model : models) {
            final String text = model.getFullName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void setupDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:

                        Intent intent = new Intent(UserListActivity.this, MainActivity.class);
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

    private void showUserList(List<User> users) {
        mUsers = users;
        mUserListAdapter = new UserListAdapter(users, new UserListAdapter.UserViewHolder.UserItemClickListener() {
            @Override
            public void onUserItemClick(int adapterPosition) {

                UserDTO userDTO = new UserDTO(mUserListAdapter.getUser(adapterPosition));
                Intent intent = new Intent(UserListActivity.this, ProfileUserActivity.class);
                intent.putExtra(ConstantManager.USER_DTO_KEY, userDTO);

                startActivity(intent);

            }
        });
        mRecyclerView.setAdapter(mUserListAdapter);
    }


    /**
     * chronos
     *
     * @param result
     */
    public void onOperationFinished(final LoadUserListFromDbTask.Result result) {
        if (result.isSuccessful()) {
            showUserList(result.getOutput());
        } else {
            showSnackbar(result.getErrorMessage());
        }
    }

}
