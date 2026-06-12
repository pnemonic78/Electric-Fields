package com.github.fields.electric.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.github.fields.electric.R
import com.github.fields.electric.compose.AppTheme
import com.github.fields.electric.compose.colorButton
import com.github.fields.electric.compose.modifierButton
import com.github.fields.electric.compose.modifierIcon
import com.github.fields.electric.compose.sizeButton
import com.github.lang.VoidCallback

@Composable
fun PaletteButton(size: Dp = sizeButton, onClick: VoidCallback) {
    IconButton(onClick = onClick, modifier = Modifier.modifierButton(size = size)) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = stringResource(R.string.menu_palette),
            tint = colorButton,
            modifier = Modifier.modifierIcon(size = size)
        )
    }
}

@Composable
@Preview
private fun Preview() {
    AppTheme {
        PaletteButton {}
    }
}