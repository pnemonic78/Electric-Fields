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
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import com.github.fields.electric.color.BlueColorMap
import com.github.fields.electric.color.ColorMap
import com.github.fields.electric.color.CyanColorMap
import com.github.fields.electric.color.GreenColorMap
import com.github.fields.electric.color.GreyColorMap
import com.github.fields.electric.color.LavaColorMap
import com.github.fields.electric.color.MagentaColorMap
import com.github.fields.electric.color.Palette
import com.github.fields.electric.color.RainbowColorMap
import com.github.fields.electric.color.RedColorMap
import com.github.fields.electric.color.YellowColorMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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
    private val palette: Palette
) : Flow<ElectricFieldImage> {

    private var painter: FieldPainter? = null
    var saturation = 1f
    var startDelay = 0L

    override suspend fun collect(collector: FlowCollector<ElectricFieldImage>) {
        painter = FieldPainter(charges, bitmap, palette, saturation, collector).apply {
            run(this@FieldsTask.startDelay)
        }
    }

    private class FieldPainter(
        val charges: List<Charge>,
        val bitmap: Bitmap,
        palette: Palette,
        private val saturation: Float,
        val collector: FlowCollector<ElectricFieldImage>
    ) {
        private val paint = Paint(ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.SQUARE
            style = Paint.Style.FILL
            strokeWidth = 1f
        }
        private val rect = RectF()
        var running = false
            private set

        private val colorMap: ColorMap = createColorMap(palette).apply {
            saturation = this@FieldPainter.saturation
        }

        suspend fun run(startDelay: Long = 0L) {
            running = true
            if (startDelay > 0L) {
                delay(startDelay)
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
            plot(charges, canvas, 0, 0, resolution, resolution)
            collector.emit(ElectricFieldImage(bitmap, Int.MAX_VALUE))

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
                        plot(charges, canvas, x1, y2, resolution, resolution)
                        plot(charges, canvas, x2, y1, resolution, resolution)
                        plot(charges, canvas, x2, y2, resolution, resolution)

                        x1 += resolution2
                        x2 += resolution2
                    } while ((x1 < w) && !isDisposed())

                    if (isDisposed()) {
                        break@loop
                    }
                    if (!BuildConfig.SAVE_FRAMES) {
                        collector.emit(ElectricFieldImage(bitmap, resolution))
                    }

                    y1 += resolution2
                    y2 += resolution2
                } while (y1 < h)

                if (BuildConfig.SAVE_FRAMES) {
                    val clone = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    collector.emit(ElectricFieldImage(clone, resolution))
                }
                resolution2 = resolution
                resolution = resolution2 shr 1
            } while ((resolution >= 1) && !isDisposed())

            running = false
            if (!isDisposed()) {
                collector.emit(ElectricFieldImage(bitmap, resolution))
            }
        }

        private fun plot(
            charges: List<Charge>,
            canvas: Canvas,
            x: Int,
            y: Int,
            w: Int,
            h: Int,
        ) {
            var dx: Int
            var dy: Int
            var d: Int
            var r: Float
            var v = 1f

            for (charge in charges) {
                dx = x - charge.x
                dy = y - charge.y
                d = (dx * dx) + (dy * dy)
                if (d == 0) {
                    //Force "overflow".
                    v = Float.POSITIVE_INFINITY
                    break
                }
                r = sqrt(d.toFloat())
                v += charge.size / r
            }

            paint.color = if (v.isInfinite() || v.isNaN()) {
                WHITE
            } else {
                v = v.coerceIn(VALUE_MIN, VALUE_MAX) / VALUE_RANGE
                colorMap.mapColor(v)
            }
            rect.set(x.toFloat(), y.toFloat(), (x + w).toFloat(), (y + h).toFloat())
            canvas.drawRect(rect, paint)
        }

        suspend fun isDisposed(): Boolean {
            val job = currentCoroutineContext()[Job] ?: return true
            return job.isCancelled || job.isCompleted
        }

        private fun createColorMap(palette: Palette): ColorMap {
            return when (palette) {
                Palette.Blue -> BlueColorMap()
                Palette.Cyan -> CyanColorMap()
                Palette.Green -> GreenColorMap()
                Palette.Grey -> GreyColorMap()
                Palette.Lava -> LavaColorMap()
                Palette.Magenta -> MagentaColorMap()
                Palette.Red -> RedColorMap()
                Palette.Yellow -> YellowColorMap()
                else -> RainbowColorMap()
            }
        }

        companion object {
            private const val VALUE_MIN = -50f
            private const val VALUE_MAX = 50f
            private const val VALUE_RANGE = VALUE_MAX - VALUE_MIN
        }
    }
}
