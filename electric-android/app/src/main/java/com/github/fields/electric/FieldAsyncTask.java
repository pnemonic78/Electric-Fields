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
public class FieldAsyncTask extends AsyncTask<Charge, Canvas, Canvas> {

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
    private final Canvas canvas;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float[] hsv = {0f, 1f, 1f};

    public FieldAsyncTask(FieldAsyncTaskListener listener, Bitmap bitmap) {
        this.listener = listener;
        this.canvas = new Canvas(bitmap);
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
    protected Canvas doInBackground(Charge... params) {
        final ChargeHolder[] charges = ChargeHolder.toChargedParticles(params);
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int size = Math.max(w, h);

        int shifts = 0;
        while (size > 1) {
            size >>>= 1;
            shifts++;
        }
        double density = 1e+3;

        // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
        int resolution2 = 1 << shifts;
        int resolution = resolution2 >> 1;

        canvas.drawColor(Color.WHITE);
        plot(charges, canvas, 0, 0, resolution, resolution, density);

        int x, y;
        int x1, y1, x2, y2;

        do {
            y = 0;

            do {
                y1 = y;
                y2 = y + resolution;
                x = resolution;

                do {
                    x1 = x - resolution;
                    x2 = x;

                    plot(charges, canvas, x1, y2, resolution, resolution, density);
                    plot(charges, canvas, x2, y1, resolution, resolution, density);
                    plot(charges, canvas, x2, y2, resolution, resolution, density);
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
        } while ((resolution >= 1) && !isCancelled());

        return canvas;
    }

    @Override
    protected void onProgressUpdate(Canvas... values) {
        super.onProgressUpdate(values);
        listener.repaint(this);
    }

    @Override
    protected void onPostExecute(Canvas result) {
        super.onPostExecute(result);
        listener.onTaskFinished(this);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onTaskCancelled(this);
    }

    private void plot(ChargeHolder[] charges, Canvas canvas, int x, int y, int w, int h, double zoom) {
        int dx, dy, d;
        double r;
        double v = 1;
        final int count = charges.length;
        ChargeHolder charge;

        for (int i = 0; i < count; i++) {
            charge = charges[i];
            dx = x - charge.x;
            dy = y - charge.y;
            d = (dx * dx) + (dy * dy);
            r = Math.sqrt(d);
            if (r == 0) {
                //Force "overflow".
                v = Double.POSITIVE_INFINITY;
                break;
            }
            v += charge.size / r;
        }

        paint.setColor(mapColor(v, zoom));
        rect.set(x, y, x + w, y + h);
        canvas.drawRect(rect, paint);
    }

    private int mapColor(double z, double density) {
        if (Double.isInfinite(z)) {
            return Color.WHITE;
        }
        hsv[0] = (float) ((z * density) % 360);
        return Color.HSVToColor(hsv);
    }

    private static class ChargeHolder {
        public final int x;
        public final int y;
        public final double size;
        public final double sizeSqr;

        public ChargeHolder(Charge charge) {
            this(charge.x, charge.y, charge.size);
        }

        public ChargeHolder(int x, int y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.sizeSqr = Math.signum(size) * size * size;
        }

        public static ChargeHolder[] toChargedParticles(Charge[] charges) {
            final int length = charges.length;
            ChargeHolder[] result = new ChargeHolder[length];

            for (int i = 0; i < length; i++) {
                result[i] = new ChargeHolder(charges[i]);
            }

            return result;
        }
    }
}
