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
import android.view.View;
import android.widget.Toast;

/**
 * Electric Fields task.
 *
 * @author Moshe Waisberg
 */
public class FieldAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {

    private final View view;
    private final Charge[] charges;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public FieldAsyncTask(View view, Charge[] charges) {
        this.view = view;
        this.charges = charges;
    }

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
        int size = Math.min(w, h);
        int resolution2 = size;
        int resolution = resolution2 / 2;
        int x = 0;
        int y = 0;
        int x1, y1, x2, y2;

        Canvas bitmapCanvas = new Canvas(bitmap);
        plot(bitmapCanvas, x, y, resolution, resolution, size);

        do {
            y = 0;

            do {
                y1 = y;
                y2 = y + resolution;
                x = resolution;

                do {
                    x1 = x - resolution;
                    x2 = x;

                    plot(bitmapCanvas, x1, y2, resolution, resolution, size);
                    plot(bitmapCanvas, x2, y1, resolution, resolution, size);
                    plot(bitmapCanvas, x2, y2, resolution, resolution, size);
                    view.postInvalidate();

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
            resolution = resolution2 / 2;
            if (isCancelled()) {
                return null;
            }
        } while ((resolution2 > 1) && !isCancelled());

        return bitmap;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        view.invalidate();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        view.invalidate();
        Toast.makeText(view.getContext(), "Finished.", Toast.LENGTH_SHORT).show();
        //TODO view.clear();
    }

    private void plot(Canvas canvas, int x, int y, int w, int h, double size) {
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
