package com.softdesign.devintensive.data.network.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * user info response model
 */

public class UserModelRes {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public class Contacts {

        @SerializedName("vk")
        @Expose
        private String vk;
        @SerializedName("phone")
        @Expose
        private String phone;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("updated")
        @Expose
        private String updated;

        public String getVk() {
            return vk;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }
    }
    public class Data {

        @SerializedName("user")
        @Expose
        private User user;
        @SerializedName("token")
        @Expose
        private String token;

        public String getToken() {
            return token;
        }


        public User getUser() {
            return user;
        }
    }

    public class ProfileValues {

        @SerializedName("homeTask")
        @Expose
        private int homeTask;
        @SerializedName("projects")
        @Expose
        private int projects;
        @SerializedName("linesCode")
        @Expose
        private int linesCode;
        @SerializedName("rait")
        @Expose
        private int rait;
        @SerializedName("updated")
        @Expose
        private String updated;

        public int getProjects() {
            return projects;
        }

        public int getLinesCode() {
            return linesCode;
        }

        public int getRating() {
            return rait;
        }
    }

    public class PublicInfo {

        public String getPhoto() {
            return photo;
        }

        @SerializedName("bio")
        @Expose

        private String bio;

        public String getBio() {
            return bio;
        }

        @SerializedName("avatar")
        @Expose
        private String avatar;
        @SerializedName("photo")
        @Expose
        private String photo;
        @SerializedName("updated")
        @Expose
        private String updated;

        public String getAvatar() {
            return avatar;
        }
    }

    public class Repo {

        @SerializedName("_id")
        @Expose
        private String id;

        public String getId() {
            return id;
        }


        public String getGit() {
            return git;
        }

        public String getTitle() {
            return title;
        }

        @SerializedName("git")
        @Expose

        private String git;
        @SerializedName("title")
        @Expose
        private String title;


    }

    public class Repositories {

        public List<Repo> getRepo() {
            return repo;
        }

        @SerializedName("repo")
        @Expose

        private List<Repo> repo = new ArrayList<Repo>();
        @SerializedName("updated")
        @Expose
        private String updated;

    }

    public class User {

        public String getFirstName() {
            return firstName;
        }

        @SerializedName("_id")
        @Expose

        private String id;
        @SerializedName("first_name")
        @Expose
        private String firstName;
        @SerializedName("second_name")
        @Expose
        private String secondName;
        @SerializedName("__v")
        @Expose
        private int v;
        @SerializedName("repositories")
        @Expose
        private Repositories repositories;
        @SerializedName("contacts")
        @Expose
        private Contacts contacts;
        @SerializedName("profileValues")
        @Expose
        private ProfileValues profileValues;
        @SerializedName("publicInfo")
        @Expose
        private PublicInfo publicInfo;
        @SerializedName("specialization")
        @Expose
        private String specialization;
        @SerializedName("role")
        @Expose
        private String role;
        @SerializedName("updated")
        @Expose
        private String updated;

        public ProfileValues getProfileValues() {
            return profileValues;
        }

        public String getId() {
            return id;
        }

        public Contacts getContacts() {
            return contacts;
        }

        public Repositories getRepositories() {
            return repositories;
        }

        public PublicInfo getPublicInfo() {
            return publicInfo;
        }

        public String getSecondName() {
            return secondName;
        }
    }

}
