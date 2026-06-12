package com.github.fields.electric.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainScreen(uiState: MainMenuState) {
//    Scaffold(
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                // Intercepts pointer events and passes them through
//                awaitPointerEventScope {
//                    while (true) {
//                        val event = awaitPointerEvent()
//                        // Doing nothing here lets the event fall through to layouts below
//                    }
//                }
//            },
//        containerColor = Color.Transparent,
//    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .padding(innerPadding)
        ) {
            MainMenu(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp),
                uiState = uiState
            )
        }
//    }
}

@Composable
@Preview(widthDp = previewWidthDp, heightDp = previewHeightDp)
private fun Preview() {
    val uiState = object : MainMenuState {
        override val isMenuVisible: StateFlow<Boolean> = MutableStateFlow(true)
        override val isRendering: StateFlow<Boolean> = MutableStateFlow(true)
        override fun onStopClick() = Unit
        override fun onStartClick() = Unit
        override fun onPaletteClick() = Unit
        override fun onShareClick() = Unit
        override fun onFullscreenClick() = Unit
        override fun onWallpaperClick() = Unit
    }

    AppTheme {
        MainScreen(uiState)
    }
}