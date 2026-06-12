package com.github.fields.electric.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
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
fun ShareButton(size: Dp = sizeButton, onClick: VoidCallback) {
    IconButton(onClick = onClick, modifier = Modifier.modifierButton(size = size)) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = stringResource(R.string.menu_share),
            tint = colorButton,
            modifier = Modifier.modifierIcon(size = size)
        )
    }
}

@Composable
@Preview
private fun Preview() {
    AppTheme {
        ShareButton {}
    }
}