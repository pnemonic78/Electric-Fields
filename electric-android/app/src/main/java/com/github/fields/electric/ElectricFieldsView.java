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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Electric Fields view.
 *
 * @author Moshe Waisberg
 */
public class ElectricFieldsView extends View implements FieldAsyncTask.FieldAsyncTaskListener {

    public static final int MAX_CHARGES = 10;

    private final List<Charge> charges = new CopyOnWriteArrayList<>();
    private Bitmap bitmap;
    private AsyncTask task;
    private int sameChargeDistance;
    private ElectricFieldsListener listener;

    public ElectricFieldsView(Context context) {
        super(context);
        init(context);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        sameChargeDistance = res.getDimensionPixelSize(R.dimen.same_charge);
        sameChargeDistance = sameChargeDistance * sameChargeDistance;
    }

    public boolean addCharge(int x, int y, double size) {
        return addCharge(new Charge(x, y, size));
    }

    public boolean addCharge(Charge charge) {
        if (charges.size() < MAX_CHARGES) {
            if (charges.add(charge)) {
                if (listener != null) {
                    listener.onChargeAdded(this, charge);
                }
                return true;
            }
        }
        return false;
    }

    public boolean invertCharge(int x, int y) {
        Charge charge = findCharge(x, y);
        if (charge != null) {
            charge.size = -charge.size;
            if (listener != null) {
                listener.onChargeInverted(this, charge);
            }
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

    /**
     * Start the task.
     */
    public void start() {
        task = new FieldAsyncTask(this, bitmap).execute(charges.toArray(new Charge[charges.size()]));
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
        if (listener != null) {
            listener.onRenderFieldStarted(this);
        }
    }

    @Override
    public void onTaskFinished(FieldAsyncTask task) {
        if (task == this.task) {
            invalidate();
            if (listener != null) {
                listener.onRenderFieldFinished(this);
            }
            clear();
        }
    }

    @Override
    public void onTaskCancelled(FieldAsyncTask task) {
        if (listener != null) {
            listener.onRenderFieldCancelled(this);
        }
    }

    @Override
    public void repaint(FieldAsyncTask task) {
        postInvalidate();
    }

    /**
     * Get the bitmap.
     *
     * @return the bitmap.
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Set the listener for events.
     *
     * @param listener the listener.
     */
    public void setElectricFieldsListener(ElectricFieldsListener listener) {
        this.listener = listener;
    }
}
