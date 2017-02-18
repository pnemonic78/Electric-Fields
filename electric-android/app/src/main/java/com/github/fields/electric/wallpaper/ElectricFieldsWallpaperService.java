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
package com.github.fields.electric.wallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.github.fields.electric.ElectricFieldsView;
import com.github.fields.electric.FieldAsyncTask;

import java.util.Random;

/**
 * Electric Fields wallpaper service.
 *
 * @author moshe.w
 */
public class ElectricFieldsWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new ElectricFieldsWallpaperEngine();
    }

    /**
     * Electric Fields wallpaper engine.
     *
     * @author moshe.w
     */
    protected class ElectricFieldsWallpaperEngine extends Engine implements
            GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener,
            FieldAsyncTask.FieldAsyncTaskListener {

        private WallpaperView fieldsView;
        private GestureDetector gestureDetector;
        private final Random random = new Random();
        private boolean drawing;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);

            Context context = ElectricFieldsWallpaperService.this;
            fieldsView = new WallpaperView(context, this);

            gestureDetector = new GestureDetector(context, this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            fieldsView.setSize(width, height);
            randomise();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            draw();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            gestureDetector.onTouchEvent(event);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                fieldsView.start();
            } else {
                fieldsView.cancel();
            }
        }

        /**
         * Add random charges.
         */
        private void randomise() {
            int w = fieldsView.getWidth();
            int h = fieldsView.getHeight();
            int count = 1 + random.nextInt(ElectricFieldsView.MAX_CHARGES);
            fieldsView.clear();
            for (int i = 0; i < count; i++) {
                fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (random.nextBoolean() ? +1 : -1) * (1 + (random.nextDouble() * 20)));
            }
            fieldsView.restart();
        }

        @Override
        public void onTaskStarted(FieldAsyncTask task) {
        }

        @Override
        public void onTaskFinished(FieldAsyncTask task) {
            randomise();
        }

        @Override
        public void onTaskCancelled(FieldAsyncTask task) {
        }

        @Override
        public void repaint(FieldAsyncTask task) {
            draw();
        }

        public void draw() {
            if (drawing) {
                return;
            }
            drawing = true;
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            if (surfaceHolder.getSurface().isValid()) {
                try {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        fieldsView.draw(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            drawing = false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
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
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            fieldClicked(e);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
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
    }
}
