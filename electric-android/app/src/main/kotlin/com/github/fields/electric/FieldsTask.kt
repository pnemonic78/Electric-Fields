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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import com.github.reactivex.DefaultDisposable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.Thread.sleep
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Electric Fields task.
 *
 * @author Moshe Waisberg
 */
class FieldsTask(
    private val charges: List<Charge>,
    private val bitmap: Bitmap,
    private val density: Double = DEFAULT_DENSITY,
    private val hues: Double = DEFAULT_HUES
) : Observable<Bitmap>(), Disposable {

    private var runner: FieldRunner? = null
    var brightness = 1f
        set(value) {
            field = value
            runner?.let { it.brightness = value }
        }
    var saturation = 1f
        set(value) {
            field = value
            runner?.let { it.saturation = value }
        }
    var startDelay = 0L
        set(value) {
            field = value
            runner?.let { it.startDelay = value }
        }

    override fun subscribeActual(observer: Observer<in Bitmap>) {
        val d = FieldRunner(charges, bitmap, density, hues, observer)
        d.brightness = brightness
        d.saturation = saturation
        d.startDelay = startDelay
        runner = d
        observer.onSubscribe(d)
        d.run()
    }

    override fun isDisposed(): Boolean {
        return runner?.isDisposed ?: false
    }

    override fun dispose() {
        runner?.dispose()
    }

    private class FieldRunner(
        val charges: List<Charge>,
        val bitmap: Bitmap,
        val density: Double,
        val hues: Double,
        val observer: Observer<in Bitmap>
    ) : DefaultDisposable() {

        private val paint = Paint(ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.SQUARE
            style = Paint.Style.FILL
            strokeWidth = 1f
        }
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

        fun run() {
            running = true
            if (startDelay > 0L) {
                try {
                    sleep(startDelay)
                } catch (ignore: InterruptedException) {
                }
            }
            if (isDisposed) {
                running = false
                return
            }

            val w = bitmap.width
            val h = bitmap.height
            var size = max(w, h)

            var shifts = 0
            while (size > 1) {
                size = size ushr 1
                shifts++
            }

            // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
            var resolution2 = 1 shl shifts
            var resolution = resolution2

            val canvas = Canvas(bitmap)
            canvas.drawColor(WHITE)
            plot(charges, canvas, 0, 0, resolution, resolution, density)
            observer.onNext(bitmap)

            var x1: Int
            var y1: Int
            var x2: Int
            var y2: Int

            loop@ do {
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

                    if (isDisposed) {
                        break@loop
                    }
                    if (!BuildConfig.SAVE_FRAMES) {
                        observer.onNext(bitmap)
                    }

                    y1 += resolution2
                    y2 += resolution2
                } while (y1 < h)

                if (BuildConfig.SAVE_FRAMES) {
                    val clone = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    observer.onNext(clone)
                }
                resolution2 = resolution
                resolution = resolution2 shr 1
            } while ((resolution >= 1) && !isDisposed)

            running = false
            if (!isDisposed) {
                observer.onNext(bitmap)
                observer.onComplete()
            }
        }

        private fun plot(
            charges: List<Charge>,
            canvas: Canvas,
            x: Int,
            y: Int,
            w: Int,
            h: Int,
            zoom: Double
        ) {
            var dx: Int
            var dy: Int
            var d: Int
            var r: Double
            var v = 1.0
            val count = charges.size
            var charge: Charge

            for (i in 0 until count) {
                charge = charges[i]
                dx = x - charge.x
                dy = y - charge.y
                d = (dx * dx) + (dy * dy)
                r = sqrt(d.toDouble())
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
            if (z.isInfinite() || z.isNaN()) {
                return WHITE
            }
            hsv[0] = ((z * density) % hues).toFloat()
            return Color.HSVToColor(hsv)
        }

        override fun onDispose() {
        }
    }

    fun cancel() {
        dispose()
    }

    fun isIdle(): Boolean = (runner == null) || !runner!!.running || isDisposed

    companion object {

        const val DEFAULT_DENSITY = 1000.0
        const val DEFAULT_HUES = 360.0

    }
}
