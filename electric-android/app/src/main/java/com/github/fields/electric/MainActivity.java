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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import java.util.Random;

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
public class MainActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener,
        ElectricFieldsListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_SAVE = 1;

    private ElectricFieldsView fieldsView;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private AsyncTask saveTask;
    private Charge chargeToScale;
    private float scaleFactor = 1f;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);
        fieldsView.setElectricFieldsListener(this);

        gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);

        scaleGestureDetector = new ScaleGestureDetector(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fieldsView.cancel();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = scaleGestureDetector.onTouchEvent(event);
        result = gestureDetector.onTouchEvent(event) || result;
        return result || super.onTouchEvent(event);
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
        if (!getActionBar().isShowing()) {
            getActionBar().show();
        }
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

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scaleFactor = 1f;
        float x = detector.getFocusX();
        float y = detector.getFocusY();
        chargeToScale = fieldsView.findCharge((int) x, (int) y);
        return chargeToScale != null;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        return chargeToScale != null;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if ((chargeToScale != null) && (scaleFactor != 1f)) {
            chargeToScale.size *= scaleFactor;
            fieldsView.restart();
        }
    }

    private void fieldClicked(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        long duration = Math.min(SystemClock.uptimeMillis() - e.getDownTime(), DateUtils.SECOND_IN_MILLIS);
        double size = 1.0 + (int) (duration / 20L);
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
            case R.id.menu_preview:
                getActionBar().hide();
                return true;
            case R.id.menu_random:
                randomise();
                return true;
            case R.id.menu_save_file:
                saveToFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Add random charges.
     */
    private void randomise() {
        int w = fieldsView.getMeasuredWidth();
        int h = fieldsView.getMeasuredHeight();
        int count = random.nextInt(ElectricFieldsView.MAX_CHARGES);
        fieldsView.clear();
        for (int i = 0; i < count; i++) {
            fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (random.nextBoolean() ? +1 : -1) * random.nextDouble() * 20);
        }
        fieldsView.restart();
    }

    /**
     * Save the bitmap to a file.
     */
    private void saveToFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Activity activity = MainActivity.this;
            if (activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_SAVE);
                return;
            }
        }

        // Busy saving?
        if ((saveTask != null) && (saveTask.getStatus() == AsyncTask.Status.RUNNING)) {
            return;
        }
        saveTask = new SaveFileTask(this).execute(fieldsView.getBitmap());
    }

    @Override
    public void onChargeAdded(ElectricFieldsView view, Charge charge) {
    }

    @Override
    public void onChargeInverted(ElectricFieldsView view, Charge charge) {
    }

    @Override
    public void onRenderFieldStarted(ElectricFieldsView view) {
    }

    @Override
    public void onRenderFieldFinished(ElectricFieldsView view) {
        if (view == fieldsView) {
            Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRenderFieldCancelled(ElectricFieldsView view) {
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_SAVE) {
            if ((permissions.length > 0) && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    saveToFile();
                    return;
                }
            }
        }
    }
}
