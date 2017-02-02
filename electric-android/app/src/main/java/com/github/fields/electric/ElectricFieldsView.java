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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Electric Fields view.
 * Created by Moshe on 2017/01/28.
 */
public class ElectricFieldsView extends View {

    private static final int DELTA_TOUCH_PX = 100;

    private final List<Charge> charges = new CopyOnWriteArrayList<>();
    private Bitmap bitmap;
    private AsyncTask task;

    public ElectricFieldsView(Context context) {
        super(context);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addCharge(int x, int y, double size) {
        addCharge(new Charge(x, y, size));
    }

    public void addCharge(Charge field) {
        charges.add(field);
    }

    public boolean invertCharge(int x, int y) {
        for (Charge charge : charges) {
            if ((Math.abs(x - charge.x) <= DELTA_TOUCH_PX) && (Math.abs(y - charge.y) <= DELTA_TOUCH_PX)) {
                charge.size = -charge.size;
                return true;
            }
        }
        return false;
    }

    public void clear() {
        charges.clear();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Bitmap bitmapOld = bitmap;
        if (bitmapOld != null) {
            int bw = bitmapOld.getWidth();
            int bh = bitmapOld.getHeight();

            if ((w != bw) || (h != bh)) {
                bitmap = Bitmap.createBitmap(bitmapOld, 0, 0, bw, bh);
                if (bitmapOld != bitmap) {
                    bitmapOld.recycle();
                }
            }
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void start() {
        cancel();
        task = new FieldAsyncTask().execute(bitmap);
    }

    public void cancel() {
        if (task != null) {
            task.cancel(true);
        }
    }

    private class FieldAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            bitmap.eraseColor(Color.WHITE);

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int x = 0;
            int y = 0;
            int resolution = Math.min(w, h);
            int resolution2 = resolution;
            resolution = resolution / 2;

            Canvas bitmapCanvas = new Canvas(bitmap);
            plot(bitmapCanvas, w, h, x, y, resolution, resolution);

            do {
                y = 0;
                do {
                    x = resolution;
                    do {
                        plot(bitmapCanvas, w, h, x, y, resolution, resolution);
                        plot(bitmapCanvas, w, h, x - resolution, y + resolution, resolution, resolution);
                        plot(bitmapCanvas, w, h, x, y + resolution, resolution, resolution);
                        postInvalidate();

                        x += resolution2;
                    } while (x <= w);

                    y += resolution2;
                } while (y <= h);

                resolution2 = resolution;
                resolution = resolution / 2;
            } while (resolution > 1);

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            invalidate();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            clear();
            invalidate();
            Toast.makeText(getContext(), "Finished.", Toast.LENGTH_SHORT).show();
        }

        protected void plot(Canvas canvas, int canvasWidth, int canvasHeight, int x, int y, int w, int h) {
            int c;
            double v = 1;
            double r, dx, dy, z;

            for (Charge charge : charges) {
                dx = x - charge.x;
                dy = y - charge.y;
                r = Math.sqrt((dx * dx) + (dy * dy));
                if (r == 0) {
                    v = 0;//Force black for "overflow".
                    break;
                }
                v += charge.size / r;
            }

            z = v * canvasHeight;
            c = filterColor(z);

            paint.setColor(c);
            rect.set(x, y, x + w, y + h);
            canvas.drawRect(rect, paint);
        }

        private int filterColor(double z) {
            //TODO z = MaxColor * ((z / MaxColor) - Round(z / MaxColor));
            int c = (int) Math.round(z);
            int r = (c & 0xF00) >> 4;
            int g = (c & 0x0F0);
            int b = (c & 0x00F) << 4;
            return Color.rgb(r, g, b);
        }
    }
}
