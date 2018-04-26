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
package com.github.fields.electric.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.SystemClock.uptimeMillis
import android.preference.PreferenceManager
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.view.GestureDetector
import android.view.MotionEvent
import com.github.fields.electric.*
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Live wallpaper view.
 *
 * @author Moshe Waisberg
 */
class WallpaperView(context: Context, listener: WallpaperListener) :
        ElectricFields,
        Observer<Bitmap>,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    var width: Int = 0
        private set
    var height: Int = 0
        private set
    private val charges: MutableList<Charge> = CopyOnWriteArrayList<Charge>()
    private var bitmap: Bitmap? = null
    private var task: FieldsTask? = null
    private var sameChargeDistance: Int = 0
    private var listener: WallpaperListener? = null
    private val gestureDetector: GestureDetector
    var idle = false
        private set
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        val res = context.resources
        sameChargeDistance = res.getDimensionPixelSize(R.dimen.same_charge)
        sameChargeDistance *= sameChargeDistance
        gestureDetector = GestureDetector(context, this)
        setWallpaperListener(listener)
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
        val count = charges.size
        var charge: Charge
        var chargeNearest: Charge? = null
        var dx: Int
        var dy: Int
        var d: Int
        var dMin = Integer.MAX_VALUE

        for (i in 0 until count) {
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

    override fun clear() {
        charges.clear()
    }

    fun draw(canvas: Canvas) {
        onDraw(canvas)
    }

    protected fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    override fun start(delay: Long) {
        if (idle) {
            val density = prefs.getInt(PaletteDialog.PREF_DENSITY, PaletteDialog.DEFAULT_DENSITY).toDouble()
            val hues = prefs.getInt(PaletteDialog.PREF_HUES, PaletteDialog.DEFAULT_HUES).toDouble()
            val observer = this
            val t = FieldsTask(charges, bitmap!!, density, hues)
            task = t
            with(t) {
                saturation = 0.5f
                brightness = 0.5f
                startDelay = delay
                subscribeOn(Schedulers.computation())
                        .subscribe(observer)
            }
        }
    }

    override fun stop() {
        task?.cancel()
        idle = true
    }

    /**
     * Set the listener for events.
     *
     * @param listener the listener.
     */
    fun setWallpaperListener(listener: WallpaperListener) {
        this.listener = listener
    }

    private fun invalidate() {
        listener?.onDraw(this)
    }

    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height

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
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        }
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

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val duration = Math.min(uptimeMillis() - e.downTime, SECOND_IN_MILLIS)
        val size = 1.0 + (duration / 20L).toDouble()
        return (listener != null) && listener!!.onRenderFieldClicked(this, x, y, size)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }

    override fun onNext(value: Bitmap) {
        invalidate()
    }

    override fun onError(e: Throwable) {
        idle = true
        listener?.onRenderFieldCancelled(this)
    }

    override fun onComplete() {
        idle = true
        listener?.onRenderFieldFinished(this)
        clear()
    }

    override fun onSubscribe(d: Disposable) {
        idle = false
        listener?.onRenderFieldStarted(this)
    }
}
