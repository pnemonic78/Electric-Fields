package com.github.fields.electric.wallpaper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;

import com.github.fields.electric.Charge;
import com.github.fields.electric.FieldAsyncTask;
import com.github.fields.electric.R;
import com.github.fields.electric.wallpaper.ElectricFieldsWallpaperService.ElectricFieldsWallpaperEngine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.fields.electric.ElectricFieldsView.MAX_CHARGES;

/**
 * Created by Moshe on 2017/02/15.
 */
public class WallpaperView implements FieldAsyncTask.FieldAsyncTaskListener {

    private int width, height;
    private final List<Charge> charges = new CopyOnWriteArrayList<>();
    private Bitmap bitmap;
    private FieldAsyncTask task;
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
        if (!isRendering()) {
            task = new FieldAsyncTask(this, new Canvas(bitmap));
            task.setSaturation(0.5f);
            task.setBrightness(0.5f);
            task.execute(charges.toArray(new Charge[charges.size()]));
        }
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
                bitmap = Bitmap.createScaledBitmap(rotated, width, height, true);
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

    /**
     * Is the task busy rendering the fields?
     *
     * @return {@code true} if rendering.
     */
    public boolean isRendering() {
        return (task != null) && !task.isCancelled() && (task.getStatus() != AsyncTask.Status.FINISHED);
    }
}
