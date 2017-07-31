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
package com.github.fields.electric

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Electric Fields view.
 *
 * @author Moshe Waisberg
 */
class ElectricFieldsView : View,
        FieldAsyncTask.FieldAsyncTaskListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {

    companion object {

        val MAX_CHARGES = 10

    }

    private val charges: MutableList<Charge> = CopyOnWriteArrayList<Charge>()
    private var bitmap: Bitmap? = null
    private var task: FieldAsyncTask? = null
    private var listener: ElectricFieldsListener? = null
    private var gestureDetector: GestureDetector? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var sameChargeDistance: Int = 0
    private var chargeToScale: Charge? = null
    private var scaleFactor = 1f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val res = context.resources
        sameChargeDistance = res.getDimensionPixelSize(R.dimen.same_charge)
        sameChargeDistance *= sameChargeDistance

        gestureDetector = GestureDetector(context, this)
        scaleGestureDetector = ScaleGestureDetector(context, this)
    }

    fun addCharge(x: Int, y: Int, size: Double): Boolean {
        return addCharge(Charge(x, y, size))
    }

    fun addCharge(charge: Charge): Boolean {
        if (charges.size < MAX_CHARGES) {
            if (charges.add(charge)) {
                if (listener != null) {
                    listener!!.onChargeAdded(this, charge)
                }
                return true
            }
        }
        return false
    }

    fun invertCharge(x: Int, y: Int): Boolean {
        val charge = findCharge(x, y)
        if (charge != null) {
            charge.size = -charge.size
            if (listener != null) {
                listener!!.onChargeInverted(this, charge)
            }
            return true
        }
        return false
    }

    fun findCharge(x: Int, y: Int): Charge? {
        val count = charges.size
        var charge: Charge
        var chargeNearest: Charge? = null
        var dx: Int
        var dy: Int
        var d: Int
        var dMin = Integer.MAX_VALUE

        for (i in 0..count - 1) {
            charge = charges[i]
            dx = x - charge.x
            dy = y - charge.y
            d = (dx * dx) + (dy * dy)
            if ((d <= sameChargeDistance) && (d < dMin)) {
                chargeNearest = charge
                dMin = d
            }
        }

        return chargeNearest
    }

    fun clear() {
        charges.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(getBitmap(), 0f, 0f, null)
    }

    /**
     * Start the task.
     */
    fun start() {
        if (!isRendering) {
            task = FieldAsyncTask(this, Canvas(getBitmap()))
            task!!.execute(*charges.toTypedArray())
        }
    }

    /**
     * Cancel the task.
     */
    fun cancel() {
        if (task != null) {
            task!!.cancel(true)
        }
    }

    /**
     * Restart the task with modified charges.
     */
    fun restart() {
        cancel()
        start()
    }

    override fun onTaskStarted(task: FieldAsyncTask) {
        if (listener != null) {
            listener!!.onRenderFieldStarted(this)
        }
    }

    override fun onTaskFinished(task: FieldAsyncTask) {
        if (task === this.task) {
            invalidate()
            if (listener != null) {
                listener!!.onRenderFieldFinished(this)
            }
            clear()
        }
    }

    override fun onTaskCancelled(task: FieldAsyncTask) {
        if (listener != null) {
            listener!!.onRenderFieldCancelled(this)
        }
    }

    override fun repaint(task: FieldAsyncTask) {
        postInvalidate()
    }

    /**
     * Get the bitmap.
     *
     * @return the bitmap.
     */
    fun getBitmap(): Bitmap {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val bitmapOld = bitmap
        if (bitmapOld != null) {
            val bw = bitmapOld.width
            val bh = bitmapOld.height

            if ((width != bw) || (height != bh)) {
                val m = Matrix()
                // Changed orientation?
                if (width < bw && height > bh) {// Portrait?
                    m.postRotate(90f, bw / 2f, bh / 2f)
                } else {// Landscape?
                    m.postRotate(270f, bw / 2f, bh / 2f)
                }
                val rotated = Bitmap.createBitmap(bitmapOld, 0, 0, bw, bh, m, true)
                bitmap = Bitmap.createScaledBitmap(rotated, width, height, true)
            }
        } else {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        return bitmap!!
    }

    /**
     * Set the listener for events.
     *
     * @param listener the listener.
     */
    fun setElectricFieldsListener(listener: ElectricFieldsListener) {
        this.listener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        if (charges.size > 0) {
            val ss = SavedState(superState)
            ss.charges = charges
            return ss
        }

        return superState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        val ss = state
        super.onRestoreInstanceState(ss.superState)

        if (ss.charges != null) {
            clear()
            for (charge in ss.charges!!) {
                addCharge(charge)
            }
            restart()
        }
    }

    /**
     * Is the task busy rendering the fields?
     *
     * @return {@code true} if rendering.
     */
    val isRendering: Boolean
        get() = (task != null) && !task!!.isCancelled && (task!!.status != AsyncTask.Status.FINISHED)

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val duration = Math.min(SystemClock.uptimeMillis() - e.downTime, DateUtils.SECOND_IN_MILLIS)
        val size = 1.0 + (duration / 20L).toDouble()
        if (listener != null && listener!!.onRenderFieldClicked(this, x, y, size)) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return true
        }
        return false
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {

    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        scaleFactor = 1f
        val x = detector.focusX.toInt()
        val y = detector.focusY.toInt()
        chargeToScale = findCharge(x, y)
        return (listener != null) && (chargeToScale != null) && listener!!.onChargeScaleBegin(this, chargeToScale!!)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        return (listener != null) && listener!!.onChargeScale(this, chargeToScale!!)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (chargeToScale != null) {
            chargeToScale!!.size *= scaleFactor
            if (listener != null) {
                listener!!.onChargeScaleEnd(this, chargeToScale!!)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = scaleGestureDetector!!.onTouchEvent(event)
        result = gestureDetector!!.onTouchEvent(event) || result
        return result || super.onTouchEvent(event)
    }

    class SavedState : View.BaseSavedState {

        internal var charges: List<Charge>? = null

        protected constructor(source: Parcel) : super(source) {
            charges = ArrayList<Charge>()
            source.readTypedList(charges, Charge.CREATOR)
        }

        constructor(superState: Parcelable) : super(superState) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedList(charges)
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(p: Parcel): SavedState {
                    return SavedState(p)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}