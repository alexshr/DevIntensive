package com.softdesign.devintensive.telephony;

import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Locale;

/**
 * Created by alexshr on 18.07.2016.
 *
 * Форматирует вводимый номер телефона по международным стандартам (как их понимает Google)
 * согласно коду страны ISO 3166-1 alpha-2 (https://ru.wikipedia.org/wiki/ISO_3166-1)
 * <p/>
 * Отсюда следует, что формат номера будет соответствовать стране,
 * а если, например, пользователь с "RU" начнет вводить не с "+7",
 * то телефон введен будет, но нормального форматирования не будет,
 * что вполне логично, т.к. номер, скорее всего, просто неверный,
 * а маска нужна именно для того, чтобы это обнаружить
 * <p/>
 * Форматирует аналогично стандартному PhoneNumberFormattingTextWatcher
 * (https://developer.android.com/reference/android/telephony/PhoneNumberFormattingTextWatcher.html),
 * однако, есть отличия, вызванные текущим заданием (devintensive):
 * 1) знак "+" в начале ввода "приклеен" намертво
 * 2) пользователь не может отменить форматирование (в стандартном классе после удаления любого символа маски форматирование отменяется)
 * 3) вводить возможно только цифры
 */
public class PhoneStrictUniversalPhoneNumberTextWatcher implements TextWatcher {

    private AsYouTypeFormatter mFormatter;

    //позиция лишнего "+" для удаления (отфильтровать не мог, т.к. "+" должен быть вначале)
    private Integer charPosToDelete;

    //избегаем зацикливания при форматировании
    private boolean mSelfChange;

    /**
     * The formatting is based on the current system locale and future locale changes
     * may not take effect on this instance.
     */
    public PhoneStrictUniversalPhoneNumberTextWatcher() {
        this(Locale.getDefault().getCountry());
    }

    /**
     * The formatting is based on the given <code>countryCode</code>.
     *
     * @param countryCode the ISO 3166-1 two-letter country code that indicates the country/region
     *                    where the phone number is being entered.
     */
    public PhoneStrictUniversalPhoneNumberTextWatcher(String countryCode) {
        if (countryCode == null) throw new IllegalArgumentException();
        mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (start != 0 && s.toString().substring(start, start + count).equals("+")) {
            charPosToDelete = start;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mSelfChange) {

            //удаляю лишний "+"
            if (charPosToDelete != null) {
                mSelfChange = true;
                s.delete(charPosToDelete, charPosToDelete + 1);
                charPosToDelete = null;
                mSelfChange = false;
            }


            if (s.length() == 0) {
                //вставляю +  в начало строки
                mSelfChange = true;
                s.append("+");
                mSelfChange = false;
            } else if (s.charAt(0) != '+') {
                //вставляю +  в начало строки
                mSelfChange = true;
                if (s.length() > 0) {
                    s.replace(0, 0, "+");
                } else {
                    s.append("+");
                }
                mSelfChange = false;
            } else {
                //форматирую
                String formatted = reformat(s, Selection.getSelectionEnd(s));
                if (formatted != null) {
                    int rememberedPos = mFormatter.getRememberedPosition();
                    mSelfChange = true;
                    s.replace(0, s.length(), formatted, 0, formatted.length());

                    // The text could be changed by other TextWatcher after we changed it. If we found the
                    // text is not the one we were expecting, just give up calling setSelection().
                    if (formatted.equals(s.toString())) {
                        Selection.setSelection(s, rememberedPos);
                    }
                    mSelfChange = false;
                }
            }
        }
    }

    private String reformat(CharSequence s, int cursor) {
        // The index of char to the leftward of the cursor.
        int curIndex = cursor - 1;
        String formatted = null;
        mFormatter.clear();
        char lastNonSeparator = 0;
        boolean hasCursor = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor);
                    hasCursor = false;
                }
                lastNonSeparator = c;
            }
            if (i == curIndex) {
                hasCursor = true;
            }
        }
        if (lastNonSeparator != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor);
        }
        return formatted;
    }

    private String getFormattedNumber(char lastNonSeparator, boolean hasCursor) {
        return hasCursor ? mFormatter.inputDigitAndRememberPosition(lastNonSeparator)
                : mFormatter.inputDigit(lastNonSeparator);
    }
}
