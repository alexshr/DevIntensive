package com.softdesign.devintensive.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexshr on 07.07.2016.
 */
public class Utils {


    /**
     * creates empty png file at SDCARD in folder Pictures file IMG_yyyyMMdd_HHmmss.png
     *
     * @param context context
     * @return file
     */
    public static File createImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".png");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    //http://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        return size.x;
    }


    /****
     * Method for Setting the Height of the ListView dynamically.
     * *** Hack to fix the issue of not showing all the items of the ListView
     * *** when placed inside a ScrollView
     * <p/>
     * based on
     * http://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        if (listAdapter.getCount() > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight = listItem.getMeasuredHeight() * (listAdapter.getCount());

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    /**
     *  snackbar with  mes
     *
     * @param layout
     * @param mes
     */

    public static void showMessageOnSnackBar(CoordinatorLayout layout, String mes,boolean isError) {
        Snackbar snackbar = Snackbar.make(layout, mes, Snackbar.LENGTH_LONG);

        Snackbar.SnackbarLayout sLayout = (Snackbar.SnackbarLayout) snackbar.getView();

        TextView txtv = (TextView) sLayout.findViewById(android.support.design.R.id.snackbar_text);

        if (isError) {
            sLayout.setBackgroundColor(Color.RED);
            txtv.setTextColor(Color.WHITE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            txtv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }else {
            txtv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snackbar.show();
    }

    public static void showErrorOnSnackBar(CoordinatorLayout layout, String mes) {
        showMessageOnSnackBar(layout,mes,true);
    }

    public static void showInfoOnSnackBar(CoordinatorLayout layout, String mes) {
        showMessageOnSnackBar(layout,mes,false);
    }
}
