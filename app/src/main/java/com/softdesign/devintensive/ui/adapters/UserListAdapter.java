package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.User;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.util.List;

/**
 * adapter for user info list
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context mContext;
    private List<User> mUserList;
    private UserViewHolder.UserItemClickListener mUserItemClickListener;

    private int mWidth;
    private int mHeight;
    private static final String LOG_TAG = ConstantManager.LOG_TAG + "_UserAdapter";

    public UserListAdapter(List<User> users, UserViewHolder.UserItemClickListener userItemClickListener) {
        mUserList = users;
        mUserItemClickListener = userItemClickListener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, parent, false);

//упрощенный ресайз с учетом того, что фото захватывает всю ширину
//TODO получить реальные размеры
        mWidth = Utils.getScreenWidth(mContext);
        mHeight = (int) (mWidth / AspectRatioImageView.ASPECT_RATIO);

        return new UserViewHolder(convertView, mUserItemClickListener);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, int position) {


        User user = mUserList.get(position);

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(user.getRating() + "");
        holder.mCodeLines.setText(user.getCodeLines() + "");
        holder.mProjects.setText(user.getProjects() + "");



        if (user.getBio() == null || user.getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getBio());
        }




        String userPhoto = user.getPhoto();
        if (userPhoto != null && userPhoto.isEmpty()) {
            userPhoto = "null";
        } else {
            final String finalUserPhoto = userPhoto;

            DataManager.getInstance().getPicasso()
                    .load(userPhoto)
                    .resize(mWidth, mHeight)  //fit дольше работает, т.к. сам измеряет ImageView, а наш измеренный заранее!!
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .error(holder.mDummy)
                    .placeholder(holder.mDummy)
                    .into(holder.userPhoto, new Callback() {

                        @Override
                        public void onSuccess() {
                            Log.d(LOG_TAG, "load from cache");
                        }

                        @Override
                        public void onError() {
                            DataManager.getInstance().getPicasso()
                                    .load(finalUserPhoto)
                                    .fit()
                                    .centerCrop()
                                    .error(holder.mDummy)
                                    .placeholder(holder.mDummy)
                                    .into(holder.userPhoto, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError() {
                                            Log.d(LOG_TAG, "Could not fetch image");
                                        }
                                    });
                        }
                    });
        }

    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }



    public User getUser(int position) {
        return mUserList.get(position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio;
        private Button mShowMore;
        protected Drawable mDummy;//если нет фото


        private UserItemClickListener mListener;

        public UserViewHolder(View itemView, UserItemClickListener userItemClickListener) {
            super(itemView);
            this.mListener = userItemClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_txt);
            mRating = (TextView) itemView.findViewById(R.id.rating);
            mCodeLines = (TextView) itemView.findViewById(R.id.lines);
            mProjects = (TextView) itemView.findViewById(R.id.projects);
            mBio = (TextView) itemView.findViewById(R.id.bio_txt);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);

            mShowMore.setOnClickListener(this);

            mDummy = userPhoto.getContext().getResources().getDrawable(R.drawable.user_bg);

        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onUserItemClick(getAdapterPosition());
            }
        }

        public interface UserItemClickListener {
            void onUserItemClick(int adapterPosition);
        }


    }

    public void swap(List<User> data) {
        mUserList.clear();
        mUserList.addAll(data);
        notifyDataSetChanged();
    }

}
