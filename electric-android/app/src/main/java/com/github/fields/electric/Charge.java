package com.github.fields.electric;

import android.graphics.Point;

/**
 * Created by Moshe on 2017/01/28.
 */

public class Charge extends Point {

    public int size;

    public Charge(int x, int y, int size) {
        super(x, y);
        this.size = size;
    }

    /**
     * Set the point's x and y coordinates, and size.
     */
    public void set(int x, int y, int size) {
        set(x, y);
        this.size = size;
    }

}
