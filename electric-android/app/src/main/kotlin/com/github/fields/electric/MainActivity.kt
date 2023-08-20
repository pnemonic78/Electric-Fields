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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import com.github.fields.electric.ElectricFieldsView.Companion.MIN_CHARGES
import com.github.reactivex.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.random.Random

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
class MainActivity : Activity(),
    ElectricFieldsListener {

    private lateinit var mainView: ElectricFieldsView
    private val disposables = CompositeDisposable()
    private val random = Random.Default
    private var menuStop: MenuItem? = null
    private var menuShare: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainView = findViewById(R.id.electric_fields)
        mainView.setElectricFieldsListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainView.stop()
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val rendering = !mainView.isIdle()

        menuStop = menu.findItem(R.id.menu_stop).also {
            it.isVisible = rendering
        }

        menuShare = menu.findItem(R.id.menu_share)?.also {
            it.isEnabled = rendering
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_stop -> {
                stop()
                return true
            }
            R.id.menu_fullscreen -> {
                if (actionBar?.isShowing == true) {
                    showFullscreen()
                } else {
                    showNormalScreen()
                }
                return true
            }
            R.id.menu_random -> {
                randomise()
                return true
            }
            R.id.menu_share -> {
                share(bitmap = mainView.bitmap)
                return true
            }
            R.id.menu_palette -> {
                choosePalette()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Add random charges.
     */
    private fun randomise() {
        val w = mainView.measuredWidth
        val h = mainView.measuredHeight
        val count = random.nextInt(MIN_CHARGES, MAX_CHARGES)
        mainView.clear()
        for (i in 0 until count) {
            mainView.addCharge(
                random.nextInt(w), random.nextInt(h), random.nextDouble(-40.0, 40.0)
            )
        }
        mainView.restart()
    }

    /**
     * Save the bitmap to a file, and then share it.
     */
    private fun share(bitmap: Bitmap) {
        // Busy sharing?
        val menuItem = menuShare ?: return
        if (!menuItem.isEnabled || !menuItem.isVisible) {
            return
        }
        menuItem.isEnabled = false

        val context: Context = this
        SaveFileTask(context, bitmap)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ uri ->
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = SaveFileTask.IMAGE_MIME
                    startActivity(Intent.createChooser(this, getString(R.string.share_title)));
                }
                menuItem.isEnabled = true
            }, { e ->
                e.printStackTrace()
                Toast.makeText(context, R.string.save_failed, Toast.LENGTH_LONG).show()
                menuItem.isEnabled = true
            })
            .addTo(disposables)
    }

    private fun save(bitmap: Bitmap) {
        val context: Context = this
        SaveFileTask(context, bitmap)
            .subscribeOn(Schedulers.io())
            .subscribe({ }, { e ->
                e.printStackTrace()
            })
            .addTo(disposables)
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
            runOnUiThread {
                menuStop?.isVisible = true
                menuShare?.isVisible = false
            }
        }
    }

    override fun onRenderFieldProgress(view: ElectricFields, field: Bitmap) {
        if (BuildConfig.SAVE_FRAMES) {
            if (view == mainView) {
                save(field)
            }
        }
    }

    override fun onRenderFieldFinished(view: ElectricFields) {
        if (view == mainView) {
            runOnUiThread {
                menuStop?.isVisible = false
                menuShare?.isVisible = true
                menuShare?.isEnabled = true
                Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRenderFieldCancelled(view: ElectricFields) {
        if (view == mainView) {
            runOnUiThread {
                menuStop?.isVisible = false
            }
        }
    }

    /**
     * Maximise the image in fullscreen mode.
     * @return `true` if screen is now fullscreen.
     */
    private fun showFullscreen(): Boolean {
        val actionBar = actionBar
        if ((actionBar != null) && actionBar.isShowing) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).also { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

            // Hide the action bar.
            actionBar.hide()
            return true
        }
        return false
    }

    /**
     * Restore the image to non-fullscreen mode.
     * @return `true` if screen was fullscreen.
     */
    private fun showNormalScreen(): Boolean {
        val actionBar = actionBar
        if (actionBar != null && !actionBar.isShowing) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).also { controller ->
                controller.show(WindowInsetsCompat.Type.systemBars())
            }

            // Show the action bar.
            actionBar.show()
            return true
        }
        return false
    }

    override fun onBackPressed() {
        if (showNormalScreen()) {
            return
        }
        super.onBackPressed()
    }

    private fun stop() {
        menuStop?.isVisible = false
        mainView.stop()
        mainView.clear()
    }

    private fun choosePalette() {
        PaletteDialog(this).show()
    }
}
