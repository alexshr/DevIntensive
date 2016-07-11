package com.softdesign.devintensive.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softdesign.devintensive.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.auth_login)
    EditText mAuthLogin;
    @BindView(R.id.email_layout)
    TextInputLayout mEmailLayout;
    @BindView(R.id.auth_password)
    EditText mAuthPassword;
    @BindView(R.id.pass_layout)
    TextInputLayout mPassLayout;
    @BindView(R.id.auth_button)
    Button mAuthButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mAuthButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.auth_button:
                this.finish();
                break;
        }
    }
}