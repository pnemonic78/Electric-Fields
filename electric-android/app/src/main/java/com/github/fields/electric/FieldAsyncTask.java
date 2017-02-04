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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;

/**
 * Electric Fields task.
 *
 * @author Moshe Waisberg
 */
public class FieldAsyncTask extends AsyncTask<Charge, Bitmap, Bitmap> {

    public interface FieldAsyncTaskListener {
        /**
         * Notify the listener that the task has started processing the charges.
         *
         * @param task the caller task.
         */
        void onTaskStarted(FieldAsyncTask task);

        /**
         * Notify the listener that the task has finished.
         *
         * @param task the caller task.
         */
        void onTaskFinished(FieldAsyncTask task);

        /**
         * Notify the listener that the task has aborted.
         *
         * @param task the caller task.
         */
        void onTaskCancelled(FieldAsyncTask task);

        /**
         * Notify the listener to repaint its bitmap.
         *
         * @param task the caller task.
         */
        void repaint(FieldAsyncTask task);
    }

    private final FieldAsyncTaskListener listener;
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public FieldAsyncTask(FieldAsyncTaskListener listener, Bitmap bitmap) {
        this.listener = listener;
        this.bitmap = bitmap;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        listener.onTaskStarted(this);
    }

    @Override
    protected Bitmap doInBackground(Charge... params) {
        final Charge[] charges = params;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int size = Math.max(w, h);

        int shifts = 0;
        while (size > 1) {
            size >>>= 1;
            shifts++;
        }

        // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
        int resolution2 = 1 << shifts;
        int resolution = resolution2 >> 1;
        int x = 0;
        int y = 0;
        int x1, y1, x2, y2;

        bitmap.eraseColor(Color.WHITE);
        Canvas bitmapCanvas = new Canvas(bitmap);
        plot(charges, bitmapCanvas, x, y, resolution, resolution, size);

        do {
            y = 0;

            do {
                y1 = y;
                y2 = y + resolution;
                x = resolution;

                do {
                    x1 = x - resolution;
                    x2 = x;

                    plot(charges, bitmapCanvas, x1, y2, resolution, resolution, size);
                    plot(charges, bitmapCanvas, x2, y1, resolution, resolution, size);
                    plot(charges, bitmapCanvas, x2, y2, resolution, resolution, size);
                    listener.repaint(this);

                    x += resolution2;
                    if (isCancelled()) {
                        return null;
                    }
                } while ((x <= w) && !isCancelled());

                y += resolution2;
                if (isCancelled()) {
                    return null;
                }
            } while ((y <= h) && !isCancelled());

            resolution2 = resolution;
            resolution = resolution2 >> 1;
            if (isCancelled()) {
                return null;
            }
        } while ((resolution2 > 1) && !isCancelled());

        return bitmap;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        listener.repaint(this);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        listener.onTaskFinished(this);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onTaskCancelled(this);
    }

    private void plot(Charge[] charges, Canvas canvas, int x, int y, int w, int h, double size) {
        double dx, dy, r;
        double v = 1;

        final int count = charges.length;
        Charge charge;
        for (int i = 0; i < count; i++) {
            charge = charges[i];
            dx = x - charge.x;
            dy = y - charge.y;
            r = Math.sqrt((dx * dx) + (dy * dy));
            if (r == 0) {
                v = 0;//Force black for "overflow".
                break;
            }
            v += charge.size / r;
        }

        paint.setColor(filterColor(v * size));
        rect.set(x, y, x + w, y + h);
        canvas.drawRect(rect, paint);
    }

    private int filterColor(double z) {
        int c = (int) Math.round(z * 10000.0);
        int r = Color.red(c);
        int g = Color.green(c);
        int b = Color.blue(c);
        return Color.rgb(r, g, b);
    }
}
