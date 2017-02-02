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

import android.graphics.Point;

/**
 * Electric charge particle.
 *
 * @author Moshe Waisberg
 */
public class Charge extends Point {

    public double size;

    public Charge(int x, int y, double size) {
        super(x, y);
        this.size = size;
    }

    /**
     * Set the point's x and y coordinates, and size.
     */
    public void set(int x, int y, double size) {
        set(x, y);
        this.size = size;
    }

}
