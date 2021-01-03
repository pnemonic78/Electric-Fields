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

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.widget.NumberPicker

/**
 * Palette preferences dialog.
 *
 * @author Moshe Waisberg
 */
class PaletteDialog(context: Context) : AlertDialog(context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val densityPicker: NumberPicker
    private val huesPicker: NumberPicker

    init {
        setTitle(R.string.palette_title)
        val view = LayoutInflater.from(context).inflate(R.layout.palette, null)
        setView(view)

        densityPicker = view.findViewById(R.id.palette_density)
        densityPicker.minValue = 100
        densityPicker.maxValue = 10000
        densityPicker.value = prefs.getInt(PREF_DENSITY, DEFAULT_DENSITY)

        huesPicker = view.findViewById(R.id.palette_hues)
        huesPicker.minValue = 10
        huesPicker.maxValue = 1000
        huesPicker.value = prefs.getInt(PREF_HUES, DEFAULT_HUES)

        setButton(BUTTON_NEGATIVE, context.getText(android.R.string.cancel)) { _, _ ->
        }
        setButton(BUTTON_POSITIVE, context.getText(android.R.string.ok)) { _, _ ->
            densityPicker.clearFocus()
            huesPicker.clearFocus()
            savePreferences()
        }
        setCancelable(true)
    }

    private fun savePreferences() {
        prefs.edit()
                .putInt(PREF_DENSITY, densityPicker.value)
                .putInt(PREF_HUES, huesPicker.value)
                .apply()
    }

    companion object {

        const val PREF_DENSITY = "density"
        const val PREF_HUES = "hues"

        const val DEFAULT_DENSITY = FieldsTask.DEFAULT_DENSITY.toInt()
        const val DEFAULT_HUES = FieldsTask.DEFAULT_HUES.toInt()

    }
}