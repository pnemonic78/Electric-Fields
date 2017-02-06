/*
 * Source file of the Remove Duplicates project.
 * Copyright (c) 2016. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * https://github.com/pnemonic78/Electric-Fields
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package com.github.fields.electric;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
public class MainActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String TAG = "MainActivity";

    private ElectricFieldsView fieldsView;
    private GestureDetector gestureDetector;
    private final DateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
    private AsyncTask saveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);

        gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fieldsView.cancel();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        openOptionsMenu();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        fieldClicked(e);
        return true;
    }

    private void fieldClicked(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        long duration = Math.min(SystemClock.uptimeMillis() - e.getDownTime(), DateUtils.SECOND_IN_MILLIS);
        double size = 1.0 + (int) (duration / 10L);
        fieldClicked(x, y, size);
    }

    private void fieldClicked(int x, int y, double size) {
        if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
            fieldsView.restart();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_file:
                saveToFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Save the bitmap to a file.
     */
    private void saveToFile() {
        // Busy saving?
        if (saveTask != null) {
            return;
        }
        saveTask = new AsyncTask<Bitmap, File, File>() {

            @Override
            protected File doInBackground(Bitmap... params) {
                File folderPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File folder = new File(folderPictures, getString(R.string.app_folder_pictures));
                folder.mkdirs();

                Bitmap bitmap = params[0];
                File file = new File(folder, "ef-" + timestampFormat.format(new Date()) + ".jpg");

                OutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Log.i(TAG, "saved to " + file);
                    return file;
                } catch (IOException e) {
                    Log.e(TAG, "save failed to " + file, e);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(File file) {
                if (file != null) {
                    saveTask = null;// Allow to save another.
                    Toast.makeText(MainActivity.this, getString(R.string.saved, file.getPath()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.save_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(fieldsView.getBitmap());
    }
}
