package com.softdesign.devintensive.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.softdesign.devintensive.R;

import java.util.regex.Pattern;

/**
 * Created by alexshr on 11.07.2016.
 */
public class ValidatedTextInputLayout extends TextInputLayout {

    private boolean mIsInEditMode;

    private EditText mEditText;

    private String mErrorHint;
    private String mRegexp;
    private String mPrefix;

    private Pattern mPattern;

    private String mTextToStore;
    private boolean mIsTextValid;

    public ValidatedTextInputLayout(Context context) {
        this(context, null);
    }

    public ValidatedTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        //mEditText=getEditText();


        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ValidatedTextInputLayout, 0, 0);

        if (a.hasValue(R.styleable.ValidatedTextInputLayout_regexp)) {
            mRegexp = a.getString(R.styleable.ValidatedTextInputLayout_regexp);
            mPattern = Pattern.compile(mRegexp, Pattern.CASE_INSENSITIVE);
        }
        if (a.hasValue(R.styleable.ValidatedTextInputLayout_errorHint)) {
            mErrorHint = a.getString(R.styleable.ValidatedTextInputLayout_errorHint);
        }
        if (a.hasValue(R.styleable.ValidatedTextInputLayout_prefix)) {
            mPrefix = a.getString(R.styleable.ValidatedTextInputLayout_prefix);
        }

        a.recycle();


/*
        //validation and errorHint
        if(mPattern!=null) {
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    input(s);
                    if (validate()) {
                        hideErrorHint();
                    } else {
                        showErrorHint();
                    }
                }
            });
        }

        //phone mask
        if(mEditText.getInputType()== InputType.TYPE_CLASS_PHONE){
            mEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }
        */
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEditText = getEditText();

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus || !mIsTextValid) {
                    showErrorHint();
                } else {
                    hideErrorHint();
                }
            }
        });

        //validation and errorHint
        if (mPattern != null) {
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    input(s);
                    if (isInEditMode())
                        if (validate()) {
                            hideErrorHint();
                        } else {
                            showErrorHint();
                        }
                }
            });
        } else {
            mIsTextValid = true;
        }

        //phone mask
        if (mEditText.getInputType() == InputType.TYPE_CLASS_PHONE) {
            mEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }

    }

    private void showErrorHint() {
        setError(mErrorHint);
    }

    private void hideErrorHint() {
        setError(null);
        setErrorEnabled(false);
    }

    private void input(Editable str) {
        mTextToStore = str.toString().trim();
        if (mPrefix != null) {
            int pos = mTextToStore.indexOf(mPrefix);
            if (pos == 0) {
                mTextToStore = mTextToStore.substring(mPrefix.length());
            }
        }

        if (mEditText.getInputType() == InputType.TYPE_CLASS_PHONE) {
            mTextToStore = mTextToStore.replaceAll("[^+0-9]", "");
        }
    }

    //not call it every time because the result is cached
    public boolean validate() {
        if (mPattern == null) {
            mIsTextValid = true;
        } else {
            mIsTextValid = mPattern.matcher(mTextToStore).matches();
        }
        return mIsTextValid;
    }

    public String getTextToStore() {
        return mTextToStore;
    }

    public String getTextForIntent() {
        if (mPrefix != null) {
            return mPrefix + mTextToStore;
        } else {
            return mTextToStore;
        }

    }

    public boolean isTextValid() {
        return mIsTextValid;
    }


    public void setInEditMode(boolean isInEditMode) {
        mIsInEditMode = isInEditMode;
        mEditText.setFocusable(isInEditMode);
        mEditText.setEnabled(isInEditMode);
        mEditText.setFocusableInTouchMode(isInEditMode);
    }

    @Override
    public boolean isInEditMode() {
        return mIsInEditMode;
    }
    /*
    public void setText(String str){
        input(Editable.Factory.getInstance().newEditable(str));

    }
*/

}
