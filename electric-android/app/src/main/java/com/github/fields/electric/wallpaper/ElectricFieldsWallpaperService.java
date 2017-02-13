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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.github.fields.electric.Charge;
import com.github.fields.electric.ElectricFieldsView;
import com.github.fields.electric.FieldAsyncTask;
import com.github.fields.electric.R;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.fields.electric.ElectricFieldsView.MAX_CHARGES;

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
            FieldAsyncTask.FieldAsyncTaskListener {

        private WallpaperView fieldsView;
        private final Random random = new Random();

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            fieldsView = new WallpaperView(ElectricFieldsWallpaperService.this, this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            fieldsView.setSize(width, height);
            randomise();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            draw();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            //TODO implement gesture detector!
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
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                fieldsView.draw(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    protected class WallpaperView implements FieldAsyncTask.FieldAsyncTaskListener {

        private int width, height;
        private final List<Charge> charges = new CopyOnWriteArrayList<>();
        private Bitmap bitmap;
        private AsyncTask task;
        private int sameChargeDistance;
        private final ElectricFieldsWallpaperEngine listener;

        public WallpaperView(Context context, ElectricFieldsWallpaperEngine listener) {
            Resources res = context.getResources();
            sameChargeDistance = res.getDimensionPixelSize(R.dimen.same_charge);
            sameChargeDistance = sameChargeDistance * sameChargeDistance;
            this.listener = listener;
        }

        public boolean addCharge(int x, int y, double size) {
            return addCharge(new Charge(x, y, size));
        }

        public boolean addCharge(Charge charge) {
            if (charges.size() < MAX_CHARGES) {
                if (charges.add(charge)) {
                    return true;
                }
            }
            return false;
        }

        public boolean invertCharge(int x, int y) {
            Charge charge = findCharge(x, y);
            if (charge != null) {
                charge.size = -charge.size;
                return true;
            }
            return false;
        }

        public Charge findCharge(int x, int y) {
            final int count = charges.size();
            Charge charge;
            Charge chargeNearest = null;
            int dx, dy, d;
            int dMin = Integer.MAX_VALUE;

            for (int i = 0; i < count; i++) {
                charge = charges.get(i);
                dx = x - charge.x;
                dy = y - charge.y;
                d = (dx * dx) + (dy * dy);
                if ((d <= sameChargeDistance) && (d < dMin)) {
                    chargeNearest = charge;
                    dMin = d;
                }
            }

            return chargeNearest;
        }

        public void clear() {
            charges.clear();
        }

        public void draw(Canvas canvas) {
            onDraw(canvas);
        }

        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        /**
         * Start the task.
         */
        public void start() {
            new FieldAsyncTask(this, new Canvas(bitmap)).execute(charges.toArray(new Charge[charges.size()]));
        }

        /**
         * Cancel the task.
         */
        public void cancel() {
            if (task != null) {
                task.cancel(true);
            }
        }

        /**
         * Restart the task with modified charges.
         */
        public void restart() {
            cancel();
            start();
        }

        @Override
        public void onTaskStarted(FieldAsyncTask task) {
        }

        @Override
        public void onTaskFinished(FieldAsyncTask task) {
            if (task == this.task) {
                listener.draw();
                clear();
            }
        }

        @Override
        public void onTaskCancelled(FieldAsyncTask task) {
        }

        @Override
        public void repaint(FieldAsyncTask task) {
            listener.draw();
        }

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;

            Bitmap bitmapOld = bitmap;
            if (bitmapOld != null) {
                int bw = bitmapOld.getWidth();
                int bh = bitmapOld.getHeight();

                if ((width != bw) || (height != bh)) {
                    Matrix m = new Matrix();
                    // Changed orientation?
                    if ((width < bw) && (height > bh)) {// Portrait?
                        m.postRotate(90, bw / 2, bh / 2);
                    } else {// Landscape?
                        m.postRotate(270, bw / 2, bh / 2);
                    }
                    Bitmap rotated = Bitmap.createBitmap(bitmapOld, 0, 0, bw, bh, m, true);
                    if (bitmapOld != rotated) {
                        bitmapOld.recycle();
                    }
                    bitmap = Bitmap.createScaledBitmap(rotated, width, height, true);
                    if (rotated != bitmap) {
                        rotated.recycle();
                    }
                }
            } else {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
