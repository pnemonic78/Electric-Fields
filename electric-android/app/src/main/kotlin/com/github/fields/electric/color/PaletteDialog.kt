package com.github.fields.electric.color

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import com.github.fields.electric.FieldsPreferences
import com.github.fields.electric.R
import com.github.fields.electric.SimpleFieldsPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Palette preferences dialog.
 *
 * @author Moshe Waisberg
 */
object PaletteDialog {
    private val labels = mapOf(
        Palette.Rainbow to R.string.palette_rainbow,
        Palette.Blue to R.string.palette_blue,
        Palette.Cyan to R.string.palette_cyan,
        Palette.Green to R.string.palette_green,
        Palette.Grey to R.string.palette_grey,
        Palette.Lava to R.string.palette_lava,
        Palette.Magenta to R.string.palette_magenta,
        Palette.Red to R.string.palette_red,
        Palette.Yellow to R.string.palette_yellow
    )

    private val colors = mapOf(
        Palette.Rainbow to Color.rgb(135, 206, 250),
        Palette.Blue to Color.BLUE,
        Palette.Cyan to Color.CYAN,
        Palette.Green to Color.GREEN,
        Palette.Grey to Color.LTGRAY,
        Palette.Lava to Color.rgb(255, 165, 0),
        Palette.Magenta to Color.MAGENTA,
        Palette.Red to Color.RED,
        Palette.Yellow to Color.YELLOW
    )

    fun show(context: Context) {
        val preferences: FieldsPreferences = SimpleFieldsPreferences(context)
        var palette = preferences.palette

        val palettes = Palette.entries
        val adapter = createAdapter(context, palettes)
        val checkedItem = palettes.indexOfFirst { it == palette }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.palette_title)
            .setSingleChoiceItems(adapter, checkedItem) { _, which ->
                val item = adapter.getItem(which) as Spannable
                palette = item.getSpans(0, 1, Palette::class.java)[0] as Palette
            }
            .setNegativeButton(context.getText(android.R.string.cancel)) { _, _ -> }
            .setPositiveButton(context.getText(android.R.string.ok)) { _, _ ->
                savePreferences(preferences, palette)
            }
            .setCancelable(true)
            .show()
    }

    private fun createAdapter(context: Context, palettes: List<Palette>): ListAdapter {
        val items = palettes.map { toPaletteItem(context, it) }

        return object : ArrayAdapter<CharSequence>(
            context,
            android.R.layout.simple_list_item_single_choice,
            items
        ) {
            override fun getItemId(position: Int): Long {
                val item = getItem(position) as? Spannable ?: return -1
                val palette = item.getSpans(0, 1, Palette::class.java)[0] as Palette
                return palette.id
            }
        }
    }

    private fun savePreferences(preferences: FieldsPreferences, palette: Palette) {
        preferences.palette = palette
    }

    private fun toPaletteItem(context: Context, palette: Palette): CharSequence {
        val label: String = context.getString(labels[palette]!!)
        val s = SpannableStringBuilder(label)
        s.setSpan(palette, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(
            ForegroundColorSpan(colors[palette]!!),
            0,
            label.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return s
    }
}