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

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_VISIBLE
import android.view.WindowManager
import android.widget.Toast
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
class MainActivity : Activity(),
        ElectricFieldsListener {

    private val REQUEST_SAVE = 1

    private lateinit var mainView: ElectricFieldsView
    private val disposables = CompositeDisposable()
    private val random = Random()
    private var menuStop: MenuItem? = null
    private var menuSave: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainView = findViewById(R.id.electric_fields)
        mainView.setElectricFieldsListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainView.stop()
        disposables.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val rendering = !mainView.isIdle()

        menuStop = menu.findItem(R.id.menu_stop)
        menuStop!!.isEnabled = rendering

        menuSave = menu.findItem(R.id.menu_save_file)
        menuSave!!.isEnabled = rendering

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_stop -> {
                stop()
                return true
            }
            R.id.menu_fullscreen -> {
                if (actionBar!!.isShowing) {
                    showFullscreen()
                } else {
                    hideFullscreen()
                }
                return true
            }
            R.id.menu_random -> {
                randomise()
                return true
            }
            R.id.menu_save_file -> {
                saveToFile()
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
        val count = 1 + random.nextInt(MAX_CHARGES)
        mainView.clear()
        for (i in 0 until count) {
            mainView.addCharge(random.nextInt(w), random.nextInt(h), (if (random.nextBoolean()) +1 else -1) * (1 + random.nextDouble() * 20))
        }
        mainView.restart()
    }

    /**
     * Save the bitmap to a file.
     */
    private fun saveToFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activity = this@MainActivity
            if (activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_SAVE)
                return
            }
        }

        // Busy saving?
        if ((menuSave == null) || !menuSave!!.isEnabled) {
            return
        }
        menuSave!!.isEnabled = false

        val context = this
        val bitmap = mainView.bitmap!!
        val task = SaveFileTask(context, bitmap)
        task.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(SaveFileObserver(context, bitmap))
        disposables.add(task)
    }

    override fun onChargeAdded(view: ElectricFields, charge: Charge) {}

    override fun onChargeInverted(view: ElectricFields, charge: Charge) {}

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
                if (menuStop != null) {
                    menuStop!!.isEnabled = true
                }
                if (menuSave != null) {
                    menuSave!!.isEnabled = true
                }
            }
        }
    }

    override fun onRenderFieldFinished(view: ElectricFields) {
        if (view == mainView) {
            runOnUiThread {
                if (menuStop != null) {
                    menuStop!!.isEnabled = false
                }
                if (menuSave != null) {
                    menuSave!!.isEnabled = true
                }
                Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRenderFieldCancelled(view: ElectricFields) {
        if (view == mainView) {
            runOnUiThread {
                if (menuStop != null) {
                    menuStop!!.isEnabled = false
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_SAVE) {
            if (permissions.isNotEmpty() && (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
                    saveToFile()
                    return
                }
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
            // Hide the status bar.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
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
    private fun hideFullscreen(): Boolean {
        val actionBar = actionBar
        if (actionBar != null && !actionBar.isShowing) {
            // Show the status bar.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_VISIBLE
            }

            // Show the action bar.
            actionBar.show()
            return true
        }
        return false
    }

    override fun onBackPressed() {
        if (hideFullscreen()) {
            return
        }
        super.onBackPressed()
    }

    private fun stop() {
        mainView.stop()
        mainView.clear()
    }
}
