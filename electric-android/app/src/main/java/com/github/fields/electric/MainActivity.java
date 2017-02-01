package com.github.fields.electric;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.Random;

public class MainActivity extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private ElectricFieldsView fieldsView;
    private final Random random = new Random();
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fieldsView = (ElectricFieldsView) findViewById(R.id.electric_fields);

        gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fieldsView.cancel();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        fieldClicked(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        fieldClicked(e);
        return true;
    }

    private void fieldClicked(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        long duration = Math.min(SystemClock.uptimeMillis() - e.getDownTime(), DateUtils.SECOND_IN_MILLIS);
        double size = 1.0 + (int) (duration / 10L);
        fieldClicked(x, y, size);
    }

    private void fieldClicked(int x, int y, double size) {
        fieldsView.cancel();
        if (!fieldsView.invertCharge(x, y)) {
            fieldsView.addCharge(x, y, size);
        }
        fieldsView.start();
    }
}
