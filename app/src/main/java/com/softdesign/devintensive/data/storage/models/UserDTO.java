package com.softdesign.devintensive.data.storage.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.managers.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * для передачи данных пользователя из списка в деталицию
 */
public class UserDTO implements Parcelable {

    private String mPhoto;
    private String mFullName;
    private String mRating;
    private String mCodeLines;
    private String mProjects;
    private String mBio;
    private List<String> mRepositories;
    private boolean mIsMyFavorite;
    private int mLikesByCount;

    public UserDTO(User userData) {
        PreferencesManager mPrefManager= DataManager.getInstance().getPreferencesManager();

        List<String> repoList = new ArrayList<>();

        mPhoto = userData.getPhoto();
        mFullName = userData.getFullName();
        mRating = String.valueOf(userData.getRating()+"");
        mCodeLines = String.valueOf(userData.getCodeLines()+"");
        mProjects = String.valueOf(userData.getProjects()+"");

        mBio = String.valueOf(userData.getBio());

        for (Repository gitLink : userData.getRepositories()) {
            repoList.add(gitLink.getRepositoryName());
        }
        mRepositories = repoList;


        for(LikesBy likesBy:userData.getLikesByList()){
           if(likesBy.getSenderRemoteId()==mPrefManager.getUserId()){
               mIsMyFavorite=true;
           }
            mLikesByCount++;
        }

    }

    protected UserDTO(Parcel in) {
        mPhoto = in.readString();
        mFullName = in.readString();
        mRating = in.readString();
        mCodeLines = in.readString();
        mProjects = in.readString();
        mBio = in.readString();
        if (in.readByte() == 0x01) {
            mRepositories = new ArrayList<String>();
            in.readList(mRepositories, String.class.getClassLoader());
        } else {
            mRepositories = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPhoto);
        dest.writeString(mFullName);
        dest.writeString(mRating);
        dest.writeString(mCodeLines);
        dest.writeString(mProjects);
        dest.writeString(mBio);
        if (mRepositories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mRepositories);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<UserDTO> CREATOR = new Creator<UserDTO>() {
        @Override
        public UserDTO createFromParcel(Parcel in) {
            return new UserDTO(in);
        }

        @Override
        public UserDTO[] newArray(int size) {
            return new UserDTO[size];
        }
    };

    public String getPhoto() {
        return mPhoto;
    }

    public String getFullName() {
        return mFullName;
    }

    public String getRating() {
        return mRating;
    }

    public String getCodeLines() {
        return mCodeLines;
    }

    public String getProjects() {
        return mProjects;
    }

    public String getBio() {
        return mBio;
    }

    public List<String> getRepositories() {
        return mRepositories;
    }

    public boolean isMyFavorite() {
        return mIsMyFavorite;
    }

    public int getLikesByCount() {
        return mLikesByCount;
    }
}
