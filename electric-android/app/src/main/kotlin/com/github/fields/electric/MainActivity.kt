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
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import java.util.*

/**
 * Main activity.
 *
 * @author Moshe Waisberg
 */
class MainActivity : Activity(),
        ElectricFieldsListener {

    private val REQUEST_SAVE = 1

    private lateinit var fieldsView: ElectricFieldsView
    private var saveTask: AsyncTask<*, *, *>? = null
    private val random = Random()
    private var menuStop: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fieldsView = findViewById(R.id.electric_fields) as ElectricFieldsView
        fieldsView.setElectricFieldsListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        fieldsView.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        menuStop = menu.findItem(R.id.menu_stop)
        menuStop!!.isEnabled = fieldsView.isRendering

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
        val w = fieldsView.measuredWidth
        val h = fieldsView.measuredHeight
        val count = 1 + random.nextInt(ElectricFieldsView.MAX_CHARGES)
        fieldsView.clear()
        for (i in 0..count - 1) {
            fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (if (random.nextBoolean()) +1 else -1) * (1 + random.nextDouble() * 20))
        }
        fieldsView.restart()
    }

    /**
     * Save the bitmap to a file.
     */
    private fun saveToFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activity = this@MainActivity
            if (activity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_SAVE)
                return
            }
        }

        // Busy saving?
        if ((saveTask) != null && saveTask!!.status == AsyncTask.Status.RUNNING) {
            return
        }
        saveTask = SaveFileTask(this).execute(fieldsView.getBitmap())
    }

    override fun onChargeAdded(view: ElectricFieldsView, charge: Charge) {}

    override fun onChargeInverted(view: ElectricFieldsView, charge: Charge) {}

    override fun onChargeScaleBegin(view: ElectricFieldsView, charge: Charge): Boolean {
        return true
    }

    override fun onChargeScale(view: ElectricFieldsView, charge: Charge): Boolean {
        return true
    }

    override fun onChargeScaleEnd(view: ElectricFieldsView, charge: Charge): Boolean {
        fieldsView.restart()
        return true
    }

    override fun onRenderFieldClicked(view: ElectricFieldsView, x: Int, y: Int, size: Double): Boolean {
        if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
            fieldsView.restart()
            return true
        }
        return false
    }

    override fun onRenderFieldStarted(view: ElectricFieldsView) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop!!.isEnabled = view.isRendering
            }
        }
    }

    override fun onRenderFieldFinished(view: ElectricFieldsView) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop!!.isEnabled = false
            }
            Toast.makeText(this, R.string.finished, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRenderFieldCancelled(view: ElectricFieldsView) {
        if (view == fieldsView) {
            if (menuStop != null) {
                menuStop!!.isEnabled = false
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_SAVE) {
            if (permissions.isNotEmpty() && (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
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
                val decorView = window.decorView
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
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
                val decorView = window.decorView
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
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
        fieldsView.cancel()
        fieldsView.clear()

        if (saveTask != null) {
            saveTask!!.cancel(true)
        }
    }
}
