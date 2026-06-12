package com.github.fields.electric.color

import android.graphics.Color
import kotlin.math.absoluteValue

class GreyColorMap : ColorMap {
    private val factor = DENSITY
    private val hsv = floatArrayOf(0f, 0f, 1f)

    override var saturation: Float
        get() = hsv[1]
        set(value) {}

    override fun mapColor(value: Float): Int {
        val light = (value.absoluteValue * factor) % 2f
        hsv[2] = light.coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    companion object {
        private const val DENSITY = 1300f
    }
}