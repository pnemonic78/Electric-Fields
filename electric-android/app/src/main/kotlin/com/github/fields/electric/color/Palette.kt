package com.github.fields.electric.color

enum class Palette(val id: Long) {
    Rainbow(0),
    Blue(240),
    Cyan(180),
    Green(120),
    Grey(2),
    Lava(30),
    Magenta(300),
    Red(360),
    Yellow(60);

    companion object {
        fun of(id: Long): Palette {
            return entries.firstOrNull { it.id == id } ?: Rainbow
        }
    }
}