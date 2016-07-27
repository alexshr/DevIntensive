package com.softdesign.devintensive.ui.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

/**
 * В стандартный AppBarLayout.ScrollingViewBehavior
 * добавлено управление размером верт паддинга плашки в зависимости от AppBarLayout offset
 * <p/>
 * p.s сделать отдельный behavior плашки не получилось,
 * т.к. не получилось сделать ее соседкой для AppBarLayout
 * <p/>
 * проще и правильнее было бы воспользоваться AppBarLayout.OnOffsetChangedListener,
 * но у учителя другое мнение
 */
class CustomScrollingBehavior extends AppBarLayout.ScrollingViewBehavior {

    private final static String LOG_TAG = ConstantManager.LOG_TAG;
    private int mTotalScrollRange = -1;

    //макс padding плашки
    private int mLayoutMaxPadding;

    public CustomScrollingBehavior(Context context, AttributeSet attrs) {
        //TODO получить max padding динамически

        //ресурс специально для плашки
        mLayoutMaxPadding = context.getResources().getDimensionPixelSize(R.dimen.user_ratings_padding_v);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {


        LinearLayout ratingLayout = (LinearLayout) ((LinearLayout) child).getChildAt(0);

        final LinearLayout.LayoutParams lp =
                (LinearLayout.LayoutParams) ratingLayout.getLayoutParams();
        AppBarLayout appBarLayout = (AppBarLayout) dependency;

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();

        if (mTotalScrollRange == -1) {
            mTotalScrollRange = appBarLayout.getTotalScrollRange();
        }

//текущее изменение высоты appBar
        int offset = -behavior.getTopAndBottomOffset();
        float padding = ((mTotalScrollRange - offset) / (float) mTotalScrollRange) * mLayoutMaxPadding;

        ratingLayout.setPadding(0, (int) padding, 0, (int) padding);


        Log.d(LOG_TAG, "RatingLayoutBehavior offset=" + offset + " total=" + mTotalScrollRange + " padding=" + padding);


        return super.onDependentViewChanged(parent, child, dependency);
    }
}