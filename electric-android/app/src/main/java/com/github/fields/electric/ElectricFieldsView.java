package com.github.fields.electric;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Electric Fields view.
 * Created by Moshe on 2017/01/28.
 */
public class ElectricFieldsView extends View {

    private final List<Charge> fields = new ArrayList<>(10);
    private Bitmap bitmap;

    public ElectricFieldsView(Context context) {
        super(context);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElectricFieldsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addField(int x, int y, int size) {
        addField(new Charge(x, y, size));
    }

    public void addField(Charge field) {
        fields.add(field);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Bitmap bitmapOld = bitmap;
        if (bitmapOld != null) {
            bitmap = Bitmap.createBitmap(bitmapOld, 0, 0, w, h);
            if (bitmapOld != bitmap) {
                bitmapOld.recycle();
            }
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    protected void paint(Bitmap bitmap) {
    }
}
