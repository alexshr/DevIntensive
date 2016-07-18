package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UserListAdapter;
import com.softdesign.devintensive.ui.fragments.UserListRetainFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkStatusChecker;
import com.softdesign.devintensive.utils.BorderedCircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity implements SearchView.OnQueryTextListener {

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

    private UserListRetainFragment mRetainFragment;

    //check if data is loaded
    private boolean mIsListDataLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        setupToolBar();
        setupDrawer();

        mRetainFragment = (UserListRetainFragment) getSupportFragmentManager().findFragmentByTag(TAG_RETAIN_FRAGMENT);
        if (mRetainFragment == null) {
            mRetainFragment = new UserListRetainFragment();
            getSupportFragmentManager().beginTransaction().add(mRetainFragment, TAG_RETAIN_FRAGMENT).commit();
        }

        //проверяем пришли ли по сети данные (а не в который раз activity открывается)
        if (mRetainFragment.getUsersList() == null) {
            loadUserList();
        } else {
            setupUsersListAdapter(mRetainFragment.getUsersList());
        }

    }

    private void setupToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText!=null) {
            final List<UserListRes.UserData> filteredModelList = filter(mRetainFragment.getUsersList(), newText);
            mUserListAdapter.setFilter(filteredModelList);
        }
        return false;
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void setupDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:

                        Intent intent = new Intent(UserListActivity.this, UserListActivity.class);
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

    private void setupUsersListAdapter(List<UserListRes.UserData> users) {
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
     * fetch user list and load to retain fragment
     */
    private void loadUserList() {
        if (NetworkStatusChecker.isNetworkAvailable(this)) {

            showProgress();

            Call<UserListRes> call = mDataManager.getUserList();
            call.enqueue(new Callback<UserListRes>() {
                @Override
                public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                    if (response.code() == 200) {

                        mRetainFragment.setUsersList(response.body().getData());
                        setupUsersListAdapter(mRetainFragment.getUsersList());

                    } else {
                        showSnackbar("Не удалось получить данные с сервера: " + response.code());
                    }
                    hideProgress();
                }

                @Override
                public void onFailure(Call<UserListRes> call, Throwable t) {
                    showSnackbar("Ошибка: " + t.getMessage());
                    hideProgress();
                }
            });

        } else {
            showSnackbar("Сеть на данный момент недоступна, попробуйте позже");

        }
    }

    private void logout() {
        startActivity(new Intent(this, LoginActivity.class));
    }


}
