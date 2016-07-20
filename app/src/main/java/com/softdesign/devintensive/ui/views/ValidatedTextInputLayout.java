package com.softdesign.devintensive.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.telephony.PhoneStrictUniversalPhoneNumberTextWatcher;
import com.softdesign.devintensive.utils.ConstantManager;

import java.util.regex.Pattern;

/**
 * Created by alexshr on 11.07.2016.
 * берет на себя валидацию, доп. обработку, маскировку, валидацию, подсказки, сообщения и пр.
 * аттрибуты описаны в attr.xml
 */
public class ValidatedTextInputLayout extends TextInputLayout {

    //TODO детальнее разобраться с обязательностью параметров и значениями по умолчанию

    private static String LOG_TAG = ConstantManager.LOG_TAG + "_" + ValidatedTextInputLayout.class.getSimpleName();

    private String PREFIX_HTTP = "http://";
    private String PREFIX_HTTPS = "https://";

    // edit mode
    private boolean mIsInEditMode;

    private EditText mEditText;

    //сообщение об ошибке под текстом
    private String mErrorMessage = "";

    //подсказка над текстом при ошибке
    private String mErrorHint = "";

    //обычная подсказка
    private String mHint = "";

    //нужно ли удалять http и https префиксы
    private boolean mIsToRemoveHttpPrefix;

    //рег. выражение для валидации
    private String mRegexp;

    //храним паттерн, чтобы не компилировать постоянно
    private Pattern mValidationPattern;

    //признак валидности
    private boolean mIsTextValid;

    //стиль текста для обычной подсказки
    private int mCaptionTextAppearance;

    //стиль текста для подробной подсказки при ошибке
    private int mErrorTextAppearance;

    private Handler uiHandler = new Handler();


    public ValidatedTextInputLayout(Context context) {
        this(context, null);
    }

    public ValidatedTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

//берем аттрибуты
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ValidatedTextInputLayout, 0, 0);

        if (a.hasValue(R.styleable.ValidatedTextInputLayout_regexp)) {
            mRegexp = a.getString(R.styleable.ValidatedTextInputLayout_regexp);
            mValidationPattern = Pattern.compile(mRegexp, Pattern.CASE_INSENSITIVE);
        }
        if (a.hasValue(R.styleable.ValidatedTextInputLayout_errorHint)) {
            mErrorHint = a.getString(R.styleable.ValidatedTextInputLayout_errorHint);
        }

        if (a.hasValue(R.styleable.ValidatedTextInputLayout_errorMessage)) {
            mErrorMessage = a.getString(R.styleable.ValidatedTextInputLayout_errorMessage);
        }

        mIsToRemoveHttpPrefix = a.getBoolean(R.styleable.ValidatedTextInputLayout_isToRemoveHttpPrefix, false);


        mCaptionTextAppearance = a.getResourceId(R.styleable.ValidatedTextInputLayout_captionTextAppearance, 0);


        mErrorTextAppearance = a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);

        mHint = getHint() + "";

        a.recycle();
    }


    private String removeHttpPrefix(String str) {
        if (str.startsWith(PREFIX_HTTPS)) {
            str = str.substring(PREFIX_HTTPS.length());
        } else if (str.startsWith(PREFIX_HTTP)) {
            str = str.substring(PREFIX_HTTP.length());
        }
        return str;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHint = getHint().toString().trim();
        mEditText = getEditText();


        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //remove prefix
                String str = ((EditText) v).getText().toString().trim();
                String str1 = str;
                if (mIsToRemoveHttpPrefix) {
                    str1 = removeHttpPrefix(str);
                    if (!str1.equals(str)) {
                        ((EditText) v).setText(str1);
                    }
                }
                if (validate(str1)) {
                    hideErrorHint();
                } else {
                    if (hasFocus) {
                        showErrorHint();
                    } else {
                        hideErrorHint();
                    }
                }
            }
        });


        if (mEditText.getInputType() == InputType.TYPE_CLASS_PHONE) {
            //для номера телефона
            mEditText.addTextChangedListener(new PhoneStrictUniversalPhoneNumberTextWatcher());
        }

        //validation and errorHint
        if (mValidationPattern != null) {
            mEditText.addTextChangedListener(new TextWatcher() {

                //избегаем зацикливания
                private boolean mSelfChange;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //Log.d(LOG_TAG, "afterTextChanged s=" + s + " start=" + start + " after=" + after);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Log.d(LOG_TAG, "onTextChanged s=" + s + " start=" + start + " before=" + before + " count=" + count);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d(LOG_TAG, "afterTextChanged s=" + s);
                    String str = s.toString().trim();
                    if (mIsToRemoveHttpPrefix) {
                        str = removeHttpPrefix(str);
                    }
                    if (validate(str)) {
                        hideErrorHint();
                    } else {
                        showErrorHint();
                    }
                }
            });
        } else {
            mIsTextValid = true;
        }


    }

    //при вооде удаляем ненужные префиксы
    public void setText(String str) {
        String res = str;
        if (mIsToRemoveHttpPrefix) {
            res = removeHttpPrefix(str);
        }
        mEditText.setText(res);
    }

    //подсказка наверху при ошибке
    private void showErrorHint() {
        if (mErrorTextAppearance != -1) {
            setHintTextAppearance(mErrorTextAppearance);
        }
        setHint(mErrorHint);

    }

    //сообщение об ошибке внизу
    private void showError() {
        setError(mErrorMessage);
    }


    private void hideError() {
        setError(null);
        setErrorEnabled(false);
    }

    private void hideErrorHint() {
        if (mCaptionTextAppearance != -1) {
            setHintTextAppearance(mCaptionTextAppearance);
        }

        setHint(mHint);

    }

    public String getText() {
        return mEditText.getText().toString().trim();
    }


    //проверяем
    private boolean validate(String str) {

        if (mValidationPattern == null) {
            mIsTextValid = true;
        } else {
            if (mEditText.getInputType() == InputType.TYPE_CLASS_PHONE) {
                str = str.replaceAll("[^+0-9]", "");
            }

            mIsTextValid = mValidationPattern.matcher(str).matches();
        }
        if (mIsTextValid) {
            hideError();
        } else {
            showError();
        }
        return mIsTextValid;
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


}
