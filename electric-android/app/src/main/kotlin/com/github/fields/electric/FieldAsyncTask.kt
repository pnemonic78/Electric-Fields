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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.github.reactivex.DefaultDisposable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Electric Fields task.
 *
 * @author Moshe Waisberg
 */
class FieldAsyncTask(val charges: Collection<Charge>, val canvas: Canvas, val listener: FieldAsyncTaskListener) : Observable<Canvas>(), Disposable {

    interface FieldAsyncTaskListener {
        /**
         * Notify the listener that the task has started processing the charges.

         * @param task the caller task.
         */
        fun onTaskStarted(task: FieldAsyncTask)

        /**
         * Notify the listener that the task has finished.

         * @param task the caller task.
         */
        fun onTaskFinished(task: FieldAsyncTask)

        /**
         * Notify the listener that the task has aborted.

         * @param task the caller task.
         */
        fun onTaskCancelled(task: FieldAsyncTask)

        /**
         * Notify the listener to repaint its bitmap.

         * @param task the caller task.
         */
        fun repaint(task: FieldAsyncTask)
    }

    private lateinit var runner: FieldRunner

    override fun subscribeActual(observer: Observer<in Canvas>) {
        val d = FieldRunner(this, charges, canvas, listener, observer)
        runner = d
        observer.onSubscribe(d)
        d.run()
    }

    override fun isDisposed(): Boolean {
        return runner.isDisposed
    }

    override fun dispose() {
        runner.dispose()
    }

    private class FieldRunner(val task: FieldAsyncTask, val params: Collection<Charge>, val canvas: Canvas, val listener: FieldAsyncTaskListener, private val observer: Observer<in Canvas>) : DefaultDisposable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private val hsv = floatArrayOf(0f, 1f, 1f)
        private var startDelay = 0L
        var running = false

        init {
            with(paint) {
                strokeCap = Paint.Cap.SQUARE
                style = Paint.Style.FILL
                strokeWidth = 1f
            }
        }

        fun run() {
            running = true
            if (startDelay > 0L) {
                try {
                    Thread.sleep(startDelay)
                } catch (ignore: InterruptedException) {
                }
            }
            if (isDisposed) {
                return
            }
            listener.onTaskStarted(task)

            val charges: Array<ChargeHolder> = ChargeHolder.toChargedParticles(params)
            val w = canvas.width
            val h = canvas.height
            var size = Math.max(w, h)

            var shifts = 0
            while (size > 1) {
                size = size ushr 1
                shifts++
            }

            val density = 1e+3

            // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
            var resolution2 = 1 shl shifts
            var resolution = resolution2

            canvas.drawColor(Color.WHITE)
            plot(charges, canvas, 0, 0, resolution, resolution, density)

            var x1: Int
            var y1: Int
            var x2: Int
            var y2: Int

            do {
                y1 = 0
                y2 = resolution

                while (y1 < h) {
                    x1 = 0
                    x2 = resolution

                    while (x1 < w) {
                        plot(charges, canvas, x1, y2, resolution, resolution, density)
                        plot(charges, canvas, x2, y1, resolution, resolution, density)
                        plot(charges, canvas, x2, y2, resolution, resolution, density)

                        x1 += resolution2
                        x2 += resolution2
                        if (isDisposed) {
                            break
                        }
                    }
                    listener.repaint(task)

                    y1 += resolution2
                    y2 += resolution2
                    if (isDisposed) {
                        break
                    }
                }

                resolution2 = resolution
                resolution = resolution2 shr 1
                if (isDisposed) {
                    break
                }
            } while (resolution >= 1)

            running = false
            if (isDisposed) {
                listener.onTaskCancelled(task)
            } else {
                listener.onTaskFinished(task)
            }
        }

        private fun plot(charges: Array<ChargeHolder>, canvas: Canvas, x: Int, y: Int, w: Int, h: Int, zoom: Double) {
            var dx: Int
            var dy: Int
            var d: Int
            var r: Double
            var v = 1.0
            val count = charges.size
            var charge: ChargeHolder

            for (i in 0 until count) {
                charge = charges[i]
                dx = x - charge.x
                dy = y - charge.y
                d = (dx * dx) + (dy * dy)
                r = Math.sqrt(d.toDouble())
                if (r == 0.0) {
                    //Force "overflow".
                    v = Double.POSITIVE_INFINITY
                    break
                }
                v += charge.size / r
            }

            paint.color = mapColor(v, zoom)
            rect.set(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat())
            canvas.drawRect(rect, paint)
        }

        private fun mapColor(z: Double, density: Double): Int {
            if (z.isInfinite()) {
                return Color.WHITE
            }
            hsv[0] = (z * density % 360).toFloat()
            return Color.HSVToColor(hsv)
        }

        /**
         * Set the HSV saturation.
         *
         * @param value a value between [0..1] inclusive.
         */
        fun setSaturation(value: Float) {
            hsv[1] = value
        }

        /**
         * Set the HSV brightness.
         *
         * @param value a value between [0..1] inclusive.
         */
        fun setBrightness(value: Float) {
            hsv[2] = value
        }

        /**
         * Set the start delay.
         *
         * @param delay the start delay, in milliseconds.
         */
        fun setStartDelay(delay: Long) {
            startDelay = delay
        }

        override fun onDispose() {
        }
    }

    /**
     * Set the HSV saturation.
     *
     * @param value a value between [0..1] inclusive.
     */
    fun setSaturation(value: Float) {
        runner.setSaturation(value)
    }

    /**
     * Set the HSV brightness.
     *
     * @param value a value between [0..1] inclusive.
     */
    fun setBrightness(value: Float) {
        runner.setBrightness(value)
    }

    /**
     * Set the start delay.
     *
     * @param delay the start delay, in milliseconds.
     */
    fun setStartDelay(delay: Long) {
        runner.setStartDelay(delay)
    }

    private class ChargeHolder(val x: Int, val y: Int, val size: Double) {

        constructor(charge: Charge) : this(charge.x, charge.y, charge.size)

        companion object {

            fun toChargedParticles(vararg charges: Charge): Array<ChargeHolder> {
                val length = charges.size
                val result = arrayOfNulls<ChargeHolder?>(length)

                for (i in 0 until length) {
                    result[i] = ChargeHolder(charges[i])
                }

                return result.requireNoNulls()
            }

            fun toChargedParticles(charges: Collection<Charge>): Array<ChargeHolder> {
                val length = charges.size
                val result = arrayOfNulls<ChargeHolder?>(length)

                for (i in 0 until length) {
                    result[i] = ChargeHolder(charges.elementAt(i))
                }

                return result.requireNoNulls()
            }
        }
    }

    fun cancel() {
        dispose()
    }

    val running: Boolean
        get() = runner.running && !runner.isDisposed
}
