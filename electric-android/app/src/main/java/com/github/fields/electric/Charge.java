/*
 * Copyright 2016, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fields.electric;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Electric charge particle.
 *
 * @author Moshe Waisberg
 */
public class Charge extends Point {

    public double size;

    private Charge() {
    }

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

    @Override
    public String toString() {
        return "Charge(" + x + ", " + y + ", " + size + ")";
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        size = in.readDouble();
    }

    public static final Parcelable.Creator<Charge> CREATOR = new Parcelable.Creator<Charge>() {

        public Charge createFromParcel(Parcel in) {
            Charge r = new Charge();
            r.readFromParcel(in);
            return r;
        }

        public Charge[] newArray(int size) {
            return new Charge[size];
        }
    };

}
