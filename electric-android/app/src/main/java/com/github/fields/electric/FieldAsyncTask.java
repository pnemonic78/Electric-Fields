/*
 * Copyright 2016, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fields.electric;

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
    private long startDelay = 0L;

    public FieldAsyncTask(FieldAsyncTaskListener listener, Canvas canvas) {
        this.listener = listener;
        this.canvas = canvas;
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
        if (startDelay > 0L) {
            try {
                Thread.sleep(startDelay);
            } catch (InterruptedException e) {
                // Ignore.
            }
        }

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
        int resolution = resolution2;

        canvas.drawColor(Color.WHITE);
        plot(charges, canvas, 0, 0, resolution, resolution, density);

        int x1, y1, x2, y2;

        do {
            y1 = 0;
            y2 = resolution;

            while (y1 < h) {
                x1 = 0;
                x2 = resolution;

                while (x1 < w) {
                    plot(charges, canvas, x1, y2, resolution, resolution, density);
                    plot(charges, canvas, x2, y1, resolution, resolution, density);
                    plot(charges, canvas, x2, y2, resolution, resolution, density);

                    x1 += resolution2;
                    x2 += resolution2;
                    if (isCancelled()) {
                        return null;
                    }
                }
                listener.repaint(this);

                y1 += resolution2;
                y2 += resolution2;
                if (isCancelled()) {
                    return null;
                }
            }

            resolution2 = resolution;
            resolution = resolution2 >> 1;
            if (isCancelled()) {
                return null;
            }
        } while (resolution >= 1);

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

    /**
     * Set the HSV saturation.
     *
     * @param value a value between [0..1] inclusive.
     */
    public void setSaturation(float value) {
        hsv[1] = value;
    }

    /**
     * Set the HSV brightness.
     *
     * @param value a value between [0..1] inclusive.
     */
    public void setBrightness(float value) {
        hsv[2] = value;
    }

    /**
     * Set the start delay.
     *
     * @param delay the start delay, in milliseconds.
     */
    public void setStartDelay(long delay) {
        startDelay = delay;
    }

    private static class ChargeHolder {
        public final int x;
        public final int y;
        public final double size;
        public final double sizeSqr;

        public ChargeHolder(Charge charge) {
            this(charge.x, charge.y, charge.getSize());
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
