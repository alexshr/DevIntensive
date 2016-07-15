package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.res.UserListRes;
import com.softdesign.devintensive.ui.views.AspectRatioImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private Context mContext;
    private List<UserListRes.UserData> mUserList;
    private UserViewHolder.UserItemClickListener mUserItemClickListener;

    public UserListAdapter(List<UserListRes.UserData> users, UserViewHolder.UserItemClickListener userItemClickListener) {
        mUserList = users;
        mUserItemClickListener = userItemClickListener;
    }

    @Override
    public UserListAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(convertView, mUserItemClickListener);
    }

    @Override
    public void onBindViewHolder(UserListAdapter.UserViewHolder holder, int position) {
        UserListRes.UserData user = mUserList.get(position);

        String userPhoto = user.getPublicInfo().getPhoto();

        Picasso.with(mContext)
                .load(userPhoto)
                .resize(mContext.getResources().getDimensionPixelSize(R.dimen.profile_image_size),
                        mContext.getResources().getDimensionPixelSize(R.dimen.profile_image_size))
                .centerCrop()
                .placeholder(mContext.getResources().getDrawable(R.drawable.user_bg))
                .error(mContext.getResources().getDrawable(R.drawable.user_bg))
                .into(holder.userPhoto);

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getProfileValues().getRating()));
        holder.mCodeLines.setText(String.valueOf(user.getProfileValues().getLinesCode()));
        holder.mProjects.setText(String.valueOf(user.getProfileValues().getProjects()));

        if (user.getPublicInfo().getBio() == null || user.getPublicInfo().getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getPublicInfo().getBio());
        }
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public void setFilter(List<UserListRes.UserData> users) {
        mUserList = new ArrayList<>();
        mUserList.addAll(users);
        notifyDataSetChanged();
    }

    public UserListRes.UserData getUser(int position) {
        return mUserList.get(position);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected AspectRatioImageView userPhoto;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio;
        private Button mShowMore;

        private UserItemClickListener mListener;

        public UserViewHolder(View itemView, UserItemClickListener userItemClickListener) {
            super(itemView);
            this.mListener = userItemClickListener;

            userPhoto = (AspectRatioImageView) itemView.findViewById(R.id.user_photo_img);
            mFullName = (TextView) itemView.findViewById(R.id.user_full_name_txt);
            mRating = (TextView) itemView.findViewById(R.id.rating_txt);
            mCodeLines = (TextView) itemView.findViewById(R.id.code_lines_txt);
            mProjects = (TextView) itemView.findViewById(R.id.projects_txt);
            mBio = (TextView) itemView.findViewById(R.id.bio_txt);
            mShowMore = (Button) itemView.findViewById(R.id.more_info_btn);

            mShowMore.setOnClickListener(this);
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
}
