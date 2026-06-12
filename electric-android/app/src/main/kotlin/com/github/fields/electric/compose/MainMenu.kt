package com.github.fields.electric.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.github.fields.electric.menu.ActionsMenuCollapsed
import com.github.fields.electric.menu.FullscreenButton
import com.github.fields.electric.menu.PaletteButton
import com.github.fields.electric.menu.ShareButton
import com.github.fields.electric.menu.StartButton
import com.github.fields.electric.menu.StopButton
import com.github.fields.electric.menu.WallpaperButton
import kotlinx.coroutines.flow.StateFlow

interface MainMenuState {
    val isMenuVisible: StateFlow<Boolean>
    val isRendering: StateFlow<Boolean>
    fun onStopClick()
    fun onStartClick()
    fun onPaletteClick()
    fun onShareClick()
    fun onFullscreenClick()
    fun onWallpaperClick()
}

@Composable
fun MainMenu(modifier: Modifier = Modifier, uiState: MainMenuState) {
    val isVisible by uiState.isMenuVisible.collectAsState(true)
    val isRendering by uiState.isRendering.collectAsState(false)

    if (!isVisible) return

    ActionsMenuCollapsed(modifier = modifier) { spacing ->
        if (isRendering) {
            StopButton { uiState.onStopClick() }
            Spacer(modifier = Modifier.size(spacing))
        } else {
            StartButton { uiState.onStartClick() }
            Spacer(modifier = Modifier.size(spacing))
            ShareButton { uiState.onShareClick() }
            Spacer(modifier = Modifier.size(spacing))
        }
        FullscreenButton { uiState.onFullscreenClick() }
        Spacer(modifier = Modifier.size(spacing))
        WallpaperButton() { uiState.onWallpaperClick() }
        Spacer(modifier = Modifier.size(spacing))
        PaletteButton { uiState.onPaletteClick() }
    }
}