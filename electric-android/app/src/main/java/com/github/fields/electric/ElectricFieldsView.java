package com.github.fields.electric;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Electric Fields view.
 * Created by Moshe on 2017/01/28.
 */
public class ElectricFieldsView extends View {

    private final List<Charge> charges = new ArrayList<>(10);
    private Bitmap bitmap;
    private final Random random = new Random();
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

    public void addField(int x, int y, int size) {
        addField(new Charge(x, y, size));
    }

    public void addField(Charge field) {
        charges.add(field);
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

    protected void plot(int x, int y) {
//        int c;
//        double v = 0;
//        double z;
//        double r, xs, ys;
//        int h, w;
//        int i = 1;
//        boolean overflow = false;
//        Charge charge;
//
//        do {
//            charge = charges.get(i);
//            xs = (x - charge.x);
//            xs = xs * (x - charge.x);
//            ys = (y - charge.y);
//            ys = ys * (y - charge.y);
//            r = xs + ys;
//            r = Math.sqrt(r);
//            if (r == 0) {
//                overflow = true;
//                break;
//            } else {
//                v = v + (charge.size / r);
//            }
//
//            i++;
//        } while (i <= charges.size());
//
//        if (!overflow) {
//            z = (v + 1) * (PY + 1);
////            z = MaxColor * ((z / MaxColor) - Round(z / MaxColor));
//            c = RGBFilter(Round(z));
//            w = Round(Res);
//            h = Round(Res);
////        with Form1.Image1.Canvas do
////            begin
////                    Lock;
////        Brush.Style:=bsSolid;
////        Brush.Color:=c;
////        Pen.Color:=c;
////        if Res > 1 then
////        Rectangle(x, y, x + w, y + h)
////        else
////        Pixels[x, y]:=c;
////        Unlock;
//        }
    }

    private int RGBFilter(int c) {
        return 0xFF000000 | (c & 0xFFFFFF);
    }

    private void calculateField() {
//
//        if (PX >= PY)
//            Res = PX / 2;
//        else
//            Res = PY / 2;
//
//        plot(0, 0);
//
//        double x;
//        double y = 0;
//        do {
//            x = Res;
//            do {
//                plot(Round(x), Round(y));
//                plot(Round(x - Res), Round(y + Res));
//                plot(Round(x), Round(y + Res));
//                postInvalidate();
//
//                x = x + (Res * 2);
////    Application.ProcessMessages;
//            } while (x <= PX);
//            y = y + (Res * 2);
////    Application.ProcessMessages;
//        } while (y >= PY);
//        Res = Res / 2;
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

    private static int Round(double a) {
        return (int) Math.round(a);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancel();
    }

    private class FieldAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {

        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

            Canvas bitmapCanvas = new Canvas(bitmap);
            paint.setColor(Color.RED);
            float x1 = bitmap.getWidth() - 100;
            float y1 = bitmap.getHeight() - 100;
            float x2 = bitmap.getWidth();
            float y2 = bitmap.getHeight();
            bitmapCanvas.drawRect(x1, y1, x2, y2, paint);
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
            invalidate();
            Toast.makeText(getContext(), "Finished.", Toast.LENGTH_SHORT).show();
        }
    }
}
