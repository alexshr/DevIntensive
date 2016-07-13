package com.softdesign.devintensive.data.network.req;

/**
 * user auth request model
 */
public class UserLoginReq {

    private String email;
    private String password;

    public UserLoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
