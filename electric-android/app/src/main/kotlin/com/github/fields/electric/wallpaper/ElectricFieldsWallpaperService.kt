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
package com.github.fields.electric.wallpaper

import android.content.Context
import android.service.wallpaper.WallpaperService
import android.text.format.DateUtils
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.github.fields.electric.Charge
import com.github.fields.electric.ElectricFields
import com.github.fields.electric.ElectricFieldsView.Companion.MAX_CHARGES
import com.github.fields.electric.ElectricFieldsView.Companion.MIN_CHARGES
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * Electric Fields wallpaper service.
 *
 * @author Moshe Waisberg
 */
class ElectricFieldsWallpaperService : WallpaperService() {

    override fun onCreateEngine(): WallpaperService.Engine {
        return ElectricFieldsWallpaperEngine()
    }

    /**
     * Electric Fields wallpaper engine.
     * @author Moshe Waisberg
     */
    private inner class ElectricFieldsWallpaperEngine : WallpaperService.Engine(), WallpaperListener {

        private lateinit var fieldsView: WallpaperView
        private val random = Random.Default
        private val isDrawing = AtomicBoolean()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            val context: Context = this@ElectricFieldsWallpaperService
            fieldsView = WallpaperView(context, this)
        }

        override fun onDestroy() {
            super.onDestroy()
            fieldsView.stop()
            fieldsView.onDestroy()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            fieldsView.setSize(width, height)
            randomise()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            fieldsView.stop()
            fieldsView.onDestroy()
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            draw(fieldsView)
        }

        override fun onTouchEvent(event: MotionEvent) {
            fieldsView.onTouchEvent(event)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                fieldsView.start()
            } else {
                fieldsView.stop()
            }
        }

        /**
         * Add random charges.
         * @param delay the start delay, in milliseconds.
         */
        private fun randomise(delay: Long = 0L) {
            val w = fieldsView.width
            val h = fieldsView.height
            val count = random.nextInt(MIN_CHARGES, MAX_CHARGES)
            fieldsView.clear()
            for (i in 0 until count) {
                fieldsView.addCharge(random.nextInt(w), random.nextInt(h), random.nextDouble(-20.0, 20.0))
            }
            fieldsView.restart(delay)
        }

        override fun onChargeAdded(view: ElectricFields, charge: Charge) {}

        override fun onChargeInverted(view: ElectricFields, charge: Charge) {}

        override fun onChargeScaleBegin(view: ElectricFields, charge: Charge): Boolean {
            return false
        }

        override fun onChargeScale(view: ElectricFields, charge: Charge): Boolean {
            return false
        }

        override fun onChargeScaleEnd(view: ElectricFields, charge: Charge): Boolean {
            return false
        }

        override fun onRenderFieldClicked(view: ElectricFields, x: Int, y: Int, size: Double): Boolean {
            if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
                fieldsView.restart()
                return true
            }
            return false
        }

        override fun onRenderFieldStarted(view: ElectricFields) {}

        override fun onRenderFieldFinished(view: ElectricFields) {
            if (view === fieldsView) {
                randomise(DELAY)
            }
        }

        override fun onRenderFieldCancelled(view: ElectricFields) {}

        override fun onDraw(view: WallpaperView) {
            if (view === fieldsView) {
                draw(view)
            }
        }

        fun draw(view: WallpaperView) {
            if (!isDrawing.compareAndSet(false, true)) {
                return
            }
            val surfaceHolder = this.surfaceHolder
            if (surfaceHolder.surface.isValid) {
                try {
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        try {
                            view.draw(canvas)
                        } finally {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isDrawing.set(false)
        }
    }

    companion object {
        /**
         * Enough time for user to admire the wallpaper before starting the next rendition.
         */
        private const val DELAY = 10L * DateUtils.SECOND_IN_MILLIS
    }
}
