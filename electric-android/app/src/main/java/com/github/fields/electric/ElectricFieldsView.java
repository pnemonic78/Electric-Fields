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
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int w = metrics.widthPixels;
        int h = metrics.heightPixels;

        Bitmap bitmapOld = bitmap;
        if (bitmapOld != null) {
            int bw = bitmapOld.getWidth();
            int bh = bitmapOld.getHeight();

            if ((w != bw) || (h != bh)) {
                Matrix m = new Matrix();
                // Changed orientation?
                if ((w < bw) && (h > bh)) {// Portrait?
                    m.postRotate(90, bw / 2, bh / 2);
                } else {// Landscape?
                    m.postRotate(270, bw / 2, bh / 2);
                }
                Bitmap rotated = Bitmap.createBitmap(bitmapOld, 0, 0, bw, bh, m, true);
                if (bitmapOld != rotated) {
                    bitmapOld.recycle();
                }
                bitmap = Bitmap.createScaledBitmap(rotated, w, h, true);
                if (rotated != bitmap) {
                    rotated.recycle();
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
        task = new FieldAsyncTask(this, new Canvas(bitmap)).execute(charges.toArray(new Charge[charges.size()]));
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

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        if (bitmap != null) {
            SavedState ss = new SavedState(superState);
            ss.bitmap = bitmap;
            return ss;
        }

        return superState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.bitmap != null) {
            this.bitmap = ss.bitmap;
        }
    }

    public static class SavedState extends BaseSavedState {

        Bitmap bitmap;

        protected SavedState(Parcel source) {
            super(source);
            bitmap = Bitmap.CREATOR.createFromParcel(source);
        }

        protected SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            bitmap.writeToParcel(out, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
