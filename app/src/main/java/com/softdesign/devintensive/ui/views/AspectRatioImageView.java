package com.softdesign.devintensive.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.softdesign.devintensive.R;


/**
 * custom image view with constant height/width ratio
 *
 */
public class AspectRatioImageView extends ImageView {

//TODO add attr for ratio

    public static final float ASPECT_RATIO = 1.78f;


    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
/*
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
        mAspectRatio = a.getFloat(R.styleable.AspectRatioImageView_aspect_ratio, DEFAULT_ASPECT_RATIO);
        a.recycle();
        */
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int newWidth;
        int newHeight;

        newWidth = getMeasuredWidth();
        newHeight = (int) (newWidth / ASPECT_RATIO);

        setMeasuredDimension(newWidth, newHeight);
    }
}
