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
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleCoroutineScope
import com.github.fields.electric.Charge
import com.github.fields.electric.ElectricFieldImage
import com.github.fields.electric.ElectricFields
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import com.github.fields.electric.FieldsPreferences
import com.github.fields.electric.FieldsTask
import com.github.fields.electric.R
import com.github.fields.electric.SimpleFieldsPreferences
import com.github.fields.electric.color.Palette
import com.github.utils.copy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Live wallpaper view.
 *
 * @author Moshe Waisberg
 */
class WallpaperView(
    context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    listener: WallpaperListener
) : ElectricFields,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    var width: Int = 0
        private set
    var height: Int = 0
        private set
    private val charges: MutableList<Charge> = ArrayList()
    private var bitmap: Bitmap? = null
    private var task: Job? = null
    private var sameChargeDistance: Int = 0
    private var listener: WallpaperListener? = null
    private val gestureDetector: GestureDetector = GestureDetector(context, this)
    private val preferences: FieldsPreferences = SimpleFieldsPreferences(context)

    init {
        val res = context.resources
        sameChargeDistance = res.getDimensionPixelSize(R.dimen.same_charge)
        sameChargeDistance *= sameChargeDistance
        setWallpaperListener(listener)
    }

    fun isIdle(): Boolean {
        val job = task ?: return true
        return job.isCancelled || job.isCompleted
    }

    override var palette: Palette
        get() = preferences.palette
        set(value) {}

    override fun addCharge(x: Int, y: Int, size: Float): Boolean {
        return addCharge(Charge(x, y, size))
    }

    override fun addCharge(charge: Charge): Boolean {
        if (charges.size < MAX_CHARGES) {
            if ((charge.size != 0f) && charges.add(charge)) {
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

    override fun start(delay: Long) {
        val bitmap = bitmap ?: return
        if (!isIdle()) return

        task = lifecycleScope.launch(Dispatchers.Default) {
            FieldsTask(charges.copy(), bitmap, palette).apply {
                saturation = 0.5f
                startDelay = delay
            }
                .flowOn(Dispatchers.Default)
                .onStart {
                    val job = currentCoroutineContext()[Job] ?: return@onStart
                    if (job === task) {
                        onTaskStart()
                    }
                }
                .onCompletion {
                    val job = currentCoroutineContext()[Job] ?: return@onCompletion
                    if (job === task) {
                        if (job.isCancelled) {
                            onTaskCancelled()
                        } else {
                            onTaskComplete()
                        }
                    }
                }
                .catch { onTaskError(it) }
                .collect {
                    val job = currentCoroutineContext()[Job] ?: return@collect
                    if (job === task) {
                        onTaskNext(it)
                    }
                }
        }
    }

    override fun stop() {
        lifecycleScope.launch {
            task?.cancel()
        }
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
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

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) = Unit

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) = Unit

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val duration = min(uptimeMillis() - e.downTime, SECOND_IN_MILLIS)
        val size = 1f + (duration / 20L)
        return (listener != null) && listener!!.onRenderFieldClicked(this, x, y, size)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    private fun onTaskNext(value: ElectricFieldImage) {
        invalidate()
    }

    private fun onTaskError(e: Throwable) {
        listener?.onRenderFieldCancelled(this)
    }

    private fun onTaskCancelled() {
        listener?.onRenderFieldCancelled(this)
    }

    private fun onTaskComplete() {
        listener?.onRenderFieldFinished(this)
    }

    private fun onTaskStart() {
        listener?.onRenderFieldStarted(this)
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
                if (rotated !== bitmapOld) bitmapOld.recycle()
                bitmap = rotated.scale(width, height)
                if (rotated !== bitmap) rotated.recycle()
            }
        } else {
            bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        }
    }

    fun draw(canvas: Canvas) {
        onDraw(canvas)
    }

    private fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }

    fun onDestroy() {
        bitmap?.recycle()
        bitmap = null
    }
}
