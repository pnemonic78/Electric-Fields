package com.github.fields.electric.color

import android.graphics.Color
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

open class HueColorMap(
    private val hueMin: Float,
    hueMax: Float,
    density: Float = DENSITY
) : ColorMap {
    private val hueRange = hueMax - hueMin
    private val factor = density * hueRange
    private val hsv = floatArrayOf(0f, 1f, 1f)

    override var saturation: Float
        get() = hsv[1]
        set(value) {
            hsv[1] = value
        }

    override fun mapColor(value: Float): Int {
        val hue = (hueMin + ((value.absoluteValue * factor) % hueRange)) % HUES
        hsv[0] = min(HUES, max(0f, hue))
        hsv[2] = if (value >= 0f) 1f else 0.5f
        return Color.HSVToColor(hsv)
    }

    companion object {
        const val HUES = 360f
        const val DENSITY = 500f
    }
}