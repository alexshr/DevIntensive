package com.softdesign.devintensive.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


}
