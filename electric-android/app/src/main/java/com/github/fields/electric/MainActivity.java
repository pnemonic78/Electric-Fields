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
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Random;

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
public class MainActivity extends Activity implements
        ElectricFieldsListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_SAVE = 1;

    private ElectricFieldsView fieldsView;
    private AsyncTask saveTask;
    private final Random random = new Random();
    private MenuItem menuStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);
        fieldsView.setElectricFieldsListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fieldsView.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        menuStop = menu.findItem(R.id.menu_stop);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_stop:
                stop();
                return true;
            case R.id.menu_fullscreen:
                if (getActionBar().isShowing()) {
                    showFullscreen();
                } else {
                    hideFullscreen();
                }
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
        int count = 1 + random.nextInt(ElectricFieldsView.MAX_CHARGES);
        fieldsView.clear();
        for (int i = 0; i < count; i++) {
            fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (random.nextBoolean() ? +1 : -1) * (1 + (random.nextDouble() * 20)));
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
    public boolean onChargeScaleBegin(ElectricFieldsView view, Charge charge) {
        return charge != null;
    }

    @Override
    public boolean onChargeScale(ElectricFieldsView view, Charge charge) {
        return charge != null;
    }

    @Override
    public boolean onChargeScaleEnd(ElectricFieldsView view, Charge charge) {
        if (charge != null) {
            fieldsView.restart();
            return true;
        }
        return false;
    }

    @Override
    public boolean onRenderFieldClicked(ElectricFieldsView view, int x, int y, double size) {
        if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
            fieldsView.restart();
            return true;
        }
        return false;
    }

    @Override
    public void onRenderFieldStarted(ElectricFieldsView view) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop.setEnabled(view.isRendering());
            }
        }
    }

    @Override
    public void onRenderFieldFinished(ElectricFieldsView view) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop.setEnabled(view.isRendering());
            }
            Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRenderFieldCancelled(ElectricFieldsView view) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop.setEnabled(view.isRendering());
            }
        }
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

    /**
     * Maximise the image in fullscreen mode.
     *
     * @return {@code true} if screen is now fullscreen.
     */
    private boolean showFullscreen() {
        ActionBar actionBar = getActionBar();
        if ((actionBar != null) && actionBar.isShowing()) {
            // Hide the status bar.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }

            // Hide the action bar.
            actionBar.hide();
            return true;
        }
        return false;
    }

    /**
     * Restore the image to non-fullscreen mode.
     *
     * @return {@code true} if screen was fullscreen.
     */
    private boolean hideFullscreen() {
        ActionBar actionBar = getActionBar();
        if ((actionBar != null) && !actionBar.isShowing()) {
            // Show the status bar.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

            // Show the action bar.
            actionBar.show();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (hideFullscreen()) {
            return;
        }
        super.onBackPressed();
    }

    private void stop() {
        fieldsView.cancel();
        fieldsView.clear();

        if (saveTask != null) {
            saveTask.cancel(true);
        }
    }
}
