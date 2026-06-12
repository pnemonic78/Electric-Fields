package com.github.fields.electric.color

import androidx.annotation.FloatRange

interface ColorMap {
    var saturation: Float
    // Negative values are darker, positive values are lighter.
    fun mapColor(@FloatRange(-1.0, 1.0) value: Float): Int
}