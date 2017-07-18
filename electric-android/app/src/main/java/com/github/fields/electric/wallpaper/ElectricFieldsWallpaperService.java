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
package com.github.fields.electric.wallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.service.wallpaper.WallpaperService;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.github.fields.electric.Charge;
import com.github.fields.electric.ElectricFieldsView;

import java.util.Random;

/**
 * Electric Fields wallpaper service.
 *
 * @author Moshe Waisberg
 */
public class ElectricFieldsWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new ElectricFieldsWallpaperEngine();
    }

    /**
     * Electric Fields wallpaper engine.
     *
     * @author Moshe Waisberg
     */
    protected class ElectricFieldsWallpaperEngine extends Engine implements
            WallpaperListener {

        /**
         * Enough time for user to admire the wallpaper before starting the next rendition.
         */
        private static final long DELAY = 10 * DateUtils.SECOND_IN_MILLIS;

        private WallpaperView fieldsView;
        private final Random random = new Random();
        private boolean drawing;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);

            Context context = ElectricFieldsWallpaperService.this;
            fieldsView = new WallpaperView(context, this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            fieldsView.setSize(width, height);
            randomise();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            fieldsView.cancel();
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            draw();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            fieldsView.onTouchEvent(event);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                fieldsView.start();
            } else {
                fieldsView.cancel();
            }
        }

        /**
         * Add random charges.
         */
        private void randomise() {
            randomise(0L);
        }

        /**
         * Add random charges.
         *
         * @param delay the start delay, in milliseconds.
         */
        private void randomise(long delay) {
            int w = fieldsView.getWidth();
            int h = fieldsView.getHeight();
            int count = 1 + random.nextInt(ElectricFieldsView.MAX_CHARGES);
            fieldsView.clear();
            for (int i = 0; i < count; i++) {
                fieldsView.addCharge(random.nextInt(w), random.nextInt(h), (random.nextBoolean() ? +1 : -1) * (1 + (random.nextDouble() * 20)));
            }
            fieldsView.restart(delay);
        }

        @Override
        public void onChargeAdded(WallpaperView view, Charge charge) {
        }

        @Override
        public void onChargeInverted(WallpaperView view, Charge charge) {
        }

        @Override
        public boolean onRenderFieldClicked(WallpaperView view, int x, int y, double size) {
            if (fieldsView.invertCharge(x, y) || fieldsView.addCharge(x, y, size)) {
                fieldsView.restart();
                return true;
            }
            return false;
        }

        @Override
        public void onRenderFieldStarted(WallpaperView view) {
        }

        @Override
        public void onRenderFieldFinished(WallpaperView view) {
            if (view == fieldsView) {
                randomise(DELAY);
            }
        }

        @Override
        public void onRenderFieldCancelled(WallpaperView view) {
        }

        @Override
        public void onDraw(WallpaperView view) {
            if (view == fieldsView) {
                draw();
            }
        }

        public void draw() {
            if (drawing) {
                return;
            }
            drawing = true;
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            if (surfaceHolder.getSurface().isValid()) {
                try {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        fieldsView.draw(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            drawing = false;
        }
    }
}
