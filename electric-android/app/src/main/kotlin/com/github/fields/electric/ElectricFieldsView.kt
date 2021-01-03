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
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock.uptimeMillis
import android.preference.PreferenceManager
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.util.AttributeSet
import android.view.*
import com.github.utils.copy
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.math.min

/**
 * Electric Fields view.
 *
 * @author Moshe Waisberg
 */
class ElectricFieldsView : View,
    ElectricFields,
    Observer<Bitmap>,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {

    private val charges: MutableList<Charge> = ArrayList()

    private val size: Point by lazy {
        val sizeValue = Point()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        // include navigation bar
        display.getRealSize(sizeValue)
        sizeValue
    }

    private var _bitmap: Bitmap? = null
    val bitmap: Bitmap
        get() {
            if (_bitmap == null) {
                val size = this.size
                val width = size.x
                val height = size.y
                _bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
            return _bitmap!!
        }
    private var task: FieldsTask? = null
    private var sameChargeDistance: Int = 0
    private var listener: ElectricFieldsListener? = null
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var chargeToScale: Charge? = null
    private var scaleFactor = 1f
    private lateinit var prefs: SharedPreferences
    private var measuredWidthDiff = 0f
    private var measuredHeightDiff = 0f

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
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measuredWidthDiff = (w - bitmap.width) / 2f
        measuredHeightDiff = (h - bitmap.height) / 2f
    }

    override fun addCharge(x: Int, y: Int, size: Double): Boolean {
        return addCharge(Charge(x, y, size))
    }

    override fun addCharge(charge: Charge): Boolean {
        if (charges.size < MAX_CHARGES) {
            if (charges.add(charge)) {
                listener?.onChargeAdded(this, charge)
                return true
            }
        }
        return false
    }

    override fun invertCharge(x: Int, y: Int): Boolean {
        val charge = findCharge(x, y)
        if (charge != null) {
            charge.size = -charge.size
            listener?.onChargeInverted(this, charge)
            return true
        }
        return false
    }

    override fun findCharge(x: Int, y: Int): Charge? {
        var chargeNearest: Charge? = null
        var dx: Int
        var dy: Int
        var d: Int
        var dMin = Integer.MAX_VALUE

        for (charge in charges) {
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

    override fun clear() {
        charges.clear()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, measuredWidthDiff, measuredHeightDiff, null)
    }

    override fun start(delay: Long) {
        if (isIdle()) {
            val density = prefs.getInt(PaletteDialog.PREF_DENSITY, PaletteDialog.DEFAULT_DENSITY).toDouble()
            val hues = prefs.getInt(PaletteDialog.PREF_HUES, PaletteDialog.DEFAULT_HUES).toDouble()
            val observer = this
            FieldsTask(charges.copy(), bitmap, density, hues).apply {
                task = this
                startDelay = delay
                subscribeOn(Schedulers.computation())
                    .subscribe(observer)
            }
        }
    }

    override fun stop() {
        task?.cancel()
    }

    /**
     * Set the listener for events.
     *
     * @param listener the listener.
     */
    fun setElectricFieldsListener(listener: ElectricFieldsListener) {
        this.listener = listener
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        if ((superState != null) && (charges.size > 0)) {
            val ss = SavedState(superState)
            ss.charges = charges
            return ss
        }

        return superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        if (state.charges != null) {
            clear()
            for (charge in state.charges!!) {
                addCharge(charge)
            }
            restart()
        }
    }

    /**
     * Is the task idle and not rendering the fields?
     * @return `true` if idle.
     */
    fun isIdle(): Boolean = (task == null) || task!!.isIdle()

    override fun onDoubleTap(e: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        /*val dx = distanceX.absoluteValue
        val dy = distanceY.absoluteValue
        if (dx >= dy) {
            //TODO change palette hues
        } else {
            //TODO change palette density
        }
        return true*/
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        scaleFactor = 1f
        val x = (detector.focusX - measuredWidthDiff).toInt()
        val y = (detector.focusY - measuredHeightDiff).toInt()
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
            listener?.onChargeScaleEnd(this, chargeToScale!!)
        }
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val x = (e.x - measuredWidthDiff).toInt()
        val y = (e.y - measuredHeightDiff).toInt()
        val duration = min(uptimeMillis() - e.downTime, SECOND_IN_MILLIS)
        val size = 1.0 + (duration / 20L).toDouble()
        if (listener?.onRenderFieldClicked(this, x, y, size) == true) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return true
        }
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = scaleGestureDetector.onTouchEvent(event)
        result = gestureDetector.onTouchEvent(event) || result
        return result || super.onTouchEvent(event)
    }

    override fun onNext(value: Bitmap) {
        postInvalidate()
    }

    override fun onError(e: Throwable) {
        listener?.onRenderFieldCancelled(this)
    }

    override fun onComplete() {
        listener?.onRenderFieldFinished(this)
        clear()
    }

    override fun onSubscribe(d: Disposable) {
        listener?.onRenderFieldStarted(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _bitmap?.recycle()
        _bitmap = null
    }

    class SavedState : BaseSavedState {

        internal var charges: MutableList<Charge>? = null

        private constructor(source: Parcel) : super(source) {
            charges = ArrayList<Charge>()
            source.readTypedList(charges!!, Charge.CREATOR)
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedList(charges)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        const val MIN_CHARGES = 2
        const val MAX_CHARGES = 10
    }
}
