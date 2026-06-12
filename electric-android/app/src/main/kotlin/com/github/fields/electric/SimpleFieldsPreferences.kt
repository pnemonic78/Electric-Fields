package com.github.fields.electric

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.github.fields.electric.color.Palette

class SimpleFieldsPreferences(context: Context) : FieldsPreferences {
    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    override var palette: Palette
        get() {
            val id = preferences.getLong(KEY_PALETTE, Palette.Rainbow.id)
            return Palette.of(id)
        }
        set(value) {
            preferences.edit { putLong(KEY_PALETTE, value.id) }
        }

    companion object {
        const val KEY_PALETTE = "palette"
    }
}