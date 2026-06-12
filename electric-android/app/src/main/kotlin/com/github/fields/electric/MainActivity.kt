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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import com.github.fields.electric.ElectricFieldsView.Companion.MIN_CHARGES
import com.github.fields.electric.PaletteDialog.Companion.DEFAULT_DENSITY
import com.github.fields.electric.PaletteDialog.Companion.DEFAULT_HUES
import com.github.fields.electric.PaletteDialog.Companion.KEY_DENSITY
import com.github.fields.electric.PaletteDialog.Companion.KEY_HUES
import com.github.fields.electric.compose.AppTheme
import com.github.fields.electric.compose.MainMenuState
import com.github.fields.electric.compose.MainScreen
import com.github.fields.electric.wallpaper.ElectricFieldsWallpaperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
class MainActivity : ComponentActivity(), ElectricFieldsListener {

    private lateinit var mainView: ElectricFieldsView
    private val random = Random.Default
    private val isNormalScreen = MutableStateFlow(true)
    private val isRendering = MutableStateFlow(false)
    private val preferences = PreferenceManager.getDefaultSharedPreferences(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            handleBackPressed()
        }

        enableEdgeToEdge(window)
        val activityView = FrameLayout(this)

        mainView = ElectricFieldsView(this).apply {
            setElectricFieldsListener(this@MainActivity)
            density = preferences.getInt(KEY_DENSITY, DEFAULT_DENSITY)
            hues = preferences.getInt(KEY_HUES, DEFAULT_HUES)
        }
        activityView.addView(mainView)

        val menuState = object : MainMenuState {
            override val isMenuVisible: StateFlow<Boolean> = this@MainActivity.isNormalScreen
            override val isRendering: StateFlow<Boolean> = this@MainActivity.isRendering

            override fun onStopClick() {
                stop()
            }

            override fun onStartClick() {
                randomise()
            }

            override fun onPaletteClick() {
                choosePalette()
            }

            override fun onShareClick() {
                val bitmap = mainView.bitmap ?: return
                share(bitmap = bitmap)
            }

            override fun onFullscreenClick() {
                toggleFullscreen()
            }

            override fun onWallpaperClick() {
                applyWallpaper()
            }
        }
        val composeView = ComposeView(this)
        composeView.setContent {
            AppTheme {
                MainScreen(uiState = menuState)
            }
        }
        activityView.addView(composeView)

        ViewCompat.setOnApplyWindowInsetsListener(activityView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the insets as padding to avoid overlap
            composeView.updatePadding(
                left = insets.left,
                top = insets.top,
                right = insets.right,
                bottom = insets.bottom,
            )

            // Return consumed insets
            WindowInsetsCompat.CONSUMED
        }

        setContentView(activityView)
    }

    override fun onDestroy() {
        mainView.stop()
        super.onDestroy()
    }

    private fun toggleFullscreen() {
        if (isNormalScreen.value) {
            showFullscreen()
        } else {
            showNormalScreen()
        }
    }

    /**
     * Add random charges.
     */
    private fun randomise() {
        val w = mainView.measuredWidth
        val h = mainView.measuredHeight
        val count = random.nextInt(MIN_CHARGES, MAX_CHARGES)
        mainView.clear()
        (0 until count).forEach { _ ->
            mainView.addCharge(
                x = random.nextInt(w),
                y = random.nextInt(h),
                size = random.nextDouble(-40.0, 40.0)
            )
        }
        mainView.density = preferences.getInt(KEY_DENSITY, DEFAULT_DENSITY)
        mainView.hues = preferences.getInt(KEY_HUES, DEFAULT_HUES)
        mainView.restart()
    }

    /**
     * Save the bitmap to a file, and then share it.
     */
    private fun share(bitmap: Bitmap) {
        val context: Context = this
        lifecycleScope.launch {
            SaveFileTask(context, bitmap)
                .flowOn(Dispatchers.IO)
                .catch { e -> onSaveFile(context, e) }
                .flowOn(Dispatchers.Main)
                .collect { uri -> onSaveFile(uri) }
        }
    }

    private fun save(bitmap: Bitmap) {
        val context: Context = this
        lifecycleScope.launch {
            SaveFileTask(context, bitmap)
                .flowOn(Dispatchers.IO)
                .collect {
                    // Do nothing with the file.
                }
        }
    }

    override fun onChargeAdded(view: ElectricFields, charge: Charge) = Unit

    override fun onChargeInverted(view: ElectricFields, charge: Charge) = Unit

    override fun onChargeScaleBegin(view: ElectricFields, charge: Charge): Boolean {
        return (view == mainView)
    }

    override fun onChargeScale(view: ElectricFields, charge: Charge): Boolean {
        return (view == mainView)
    }

    override fun onChargeScaleEnd(view: ElectricFields, charge: Charge): Boolean {
        if (view == mainView) {
            view.restart()
            return true
        }
        return false
    }

    override fun onRenderFieldClicked(view: ElectricFields, x: Int, y: Int, size: Double): Boolean {
        if ((view == mainView) && (view.invertCharge(x, y) || view.addCharge(x, y, size))) {
            view.restart()
            return true
        }
        return false
    }

    override fun onRenderFieldStarted(view: ElectricFields) {
        if (view == mainView) {
            isRendering.update { true }
        }
    }

    override fun onRenderFieldProgress(view: ElectricFields, field: ElectricFieldImage) {
        if (BuildConfig.SAVE_FRAMES) {
            if (view == mainView) {
                save(field.bitmap)
            }
        }
    }

    override fun onRenderFieldFinished(view: ElectricFields) {
        if (view == mainView) {
            isRendering.update { false }
            runOnUiThread {
                Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRenderFieldCancelled(view: ElectricFields) {
        if (view == mainView) {
            isRendering.update { false }
        }
    }

    /**
     * Maximize the image in fullscreen mode.
     * @return `true` if screen is now fullscreen.
     */
    private fun showFullscreen(): Boolean {
        if (isNormalScreen.value) {
            val window = window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).also { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // Hide the menu.
            isNormalScreen.update { false }
            return true
        }
        return false
    }

    /**
     * Restore the image to non-fullscreen mode.
     * @return `true` if screen was fullscreen.
     */
    private fun showNormalScreen(): Boolean {
        if (!isNormalScreen.value) {
            val window = window
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).also { controller ->
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            // Show the menu.
            isNormalScreen.update { true }
            return true
        }
        return false
    }

    private fun handleBackPressed() {
        if (showNormalScreen()) {
            return
        }
        finish()
    }

    private fun stop() {
        mainView.stop()
        mainView.clear()
        isRendering.update { false }
    }

    private fun choosePalette() {
        PaletteDialog(this).show()
    }

    private fun onSaveFile(uri: Uri) {
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = SaveFileTask.IMAGE_MIME
            startActivity(Intent.createChooser(this, getString(R.string.share_title)))
        }
    }

    private fun onSaveFile(context: Context, error: Throwable) {
        Timber.e(error)
        Toast.makeText(context, R.string.save_failed, Toast.LENGTH_LONG).show()
    }

    private fun applyWallpaper() {
        ElectricFieldsWallpaperManager.apply(this)
    }
}
