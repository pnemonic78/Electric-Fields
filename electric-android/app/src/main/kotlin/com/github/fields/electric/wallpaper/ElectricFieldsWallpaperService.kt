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

import android.service.wallpaper.WallpaperService
import android.text.format.DateUtils
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.github.fields.electric.Charge
import com.github.fields.electric.ElectricFieldsView
import java.util.*

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
    protected inner class ElectricFieldsWallpaperEngine : WallpaperService.Engine(), WallpaperListener {

        /**
         * Enough time for user to admire the wallpaper before starting the next rendition.
         */
        private val DELAY = 10 * DateUtils.SECOND_IN_MILLIS

        private lateinit var fieldsView: WallpaperView
        private val random = Random()
        private var drawing: Boolean = false

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            val context = this@ElectricFieldsWallpaperService
            fieldsView = WallpaperView(context, this)
        }

        override fun onDestroy() {
            super.onDestroy()
            fieldsView.cancel()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            fieldsView.setSize(width, height)
            randomise()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            fieldsView.cancel()
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            draw()
        }

        override fun onTouchEvent(event: MotionEvent) {
            fieldsView.onTouchEvent(event)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                fieldsView.start()
            } else {
                fieldsView.cancel()
            }
        }

        /**
         * Add random charges.

         * @param delay the start delay, in milliseconds.
         */
        private fun randomise(delay: Long = 0L) {
            val w = fieldsView.width
            val h = fieldsView.height
            val count = 1 + random.nextInt(ElectricFieldsView.MAX_CHARGES)
            fieldsView.clear()
            for (i in 0..count - 1) {
                fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (if (random.nextBoolean()) +1 else -1) * (1 + random.nextDouble() * 20))
            }
            fieldsView.restart(delay)
        }

        override fun onChargeAdded(view: WallpaperView, charge: Charge) {}

        override fun onChargeInverted(view: WallpaperView, charge: Charge) {}

        override fun onRenderFieldClicked(view: WallpaperView, x: Int, y: Int, size: Double): Boolean {
            if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
                fieldsView.restart()
                return true
            }
            return false
        }

        override fun onRenderFieldStarted(view: WallpaperView) {}

        override fun onRenderFieldFinished(view: WallpaperView) {
            if (view === fieldsView) {
                randomise(DELAY)
            }
        }

        override fun onRenderFieldCancelled(view: WallpaperView) {}

        override fun onDraw(view: WallpaperView) {
            if (view === fieldsView) {
                draw()
            }
        }

        fun draw() {
            if (drawing) {
                return
            }
            drawing = true
            val surfaceHolder = surfaceHolder
            if (surfaceHolder.surface.isValid) {
                try {
                    val canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        try {
                            fieldsView.draw(canvas)
                        } finally {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            drawing = false
        }
    }
}
