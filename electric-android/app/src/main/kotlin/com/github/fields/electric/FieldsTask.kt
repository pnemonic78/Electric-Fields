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

import android.graphics.*
import com.github.reactivex.DefaultDisposable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Electric Fields task.
 *
 * @author Moshe Waisberg
 */
class FieldsTask(val charges: Collection<Charge>, val bitmap: Bitmap) : Observable<Bitmap>(), Disposable {

    private var runner: FieldRunner? = null
    var brightness = 1f
        set(value) {
            field = value
            if (runner != null) {
                runner!!.brightness = value
            }
        }
    var saturation = 1f
        set(value) {
            field = value
            if (runner != null) {
                runner!!.saturation = value
            }
        }
    var startDelay = 0L
        set(value) {
            field = value
            if (runner != null) {
                runner!!.startDelay = value
            }
        }

    override fun subscribeActual(observer: Observer<in Bitmap>) {
        val d = FieldRunner(charges, bitmap, observer)
        d.brightness = brightness
        d.saturation = saturation
        d.startDelay = startDelay
        runner = d
        observer.onSubscribe(d)
        d.run()
    }

    override fun isDisposed(): Boolean {
        if (runner != null) {
            return runner!!.isDisposed
        }
        return false
    }

    override fun dispose() {
        if (runner != null) {
            runner!!.dispose()
        }
    }

    private class FieldRunner(val params: Collection<Charge>, val bitmap: Bitmap, val observer: Observer<in Bitmap>) : DefaultDisposable() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private val hsv = floatArrayOf(0f, 1f, 1f)
        var startDelay = 0L
        var running = false
            private set

        var saturation: Float
            get() = hsv[1]
            set(value) {
                hsv[1] = value
            }
        var brightness: Float
            get() = hsv[2]
            set(value) {
                hsv[2] = value
            }

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
            observer.onNext(bitmap)

            val charges: Array<ChargeHolder> = ChargeHolder.toChargedParticles(params)
            val w = bitmap.width
            val h = bitmap.height
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

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            plot(charges, canvas, 0, 0, resolution, resolution, density)

            var x1: Int
            var y1: Int
            var x2: Int
            var y2: Int

            do {
                y1 = 0
                y2 = resolution

                do {
                    x1 = 0
                    x2 = resolution

                    do {
                        plot(charges, canvas, x1, y2, resolution, resolution, density)
                        plot(charges, canvas, x2, y1, resolution, resolution, density)
                        plot(charges, canvas, x2, y2, resolution, resolution, density)

                        x1 += resolution2
                        x2 += resolution2
                    } while ((x1 < w) && !isDisposed)

                    observer.onNext(bitmap)

                    y1 += resolution2
                    y2 += resolution2
                } while ((y1 < h) && !isDisposed)

                resolution2 = resolution
                resolution = resolution2 shr 1
            } while ((resolution >= 1) && !isDisposed)

            running = false
            if (!isDisposed) {
                observer.onComplete()
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

        override fun onDispose() {
        }
    }

    private class ChargeHolder(val x: Int, val y: Int, val size: Double) {

        constructor(charge: Charge) : this(charge.x, charge.y, charge.size)

        companion object {

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

    fun isIdle(): Boolean = (runner == null) || !runner!!.running || isDisposed
}
