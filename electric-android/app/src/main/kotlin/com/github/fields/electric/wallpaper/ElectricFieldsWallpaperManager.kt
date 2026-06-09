package com.github.fields.electric.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import java.lang.ref.WeakReference

object ElectricFieldsWallpaperManager {
    private var context: WeakReference<Context>? = null

    fun initialize(context: Context): ElectricFieldsWallpaperManager {
        this.context = WeakReference(context.applicationContext)
        return this
    }

    fun apply() {
        val context = context?.get() ?: return
        apply(context)
    }

    fun apply(context: Context) {
        val manager = WallpaperManager.getInstance(context)
        if (!manager.isWallpaperSupported) return
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && !manager.isSetWallpaperAllowed) {
            return
        }
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            .putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, ElectricFieldsWallpaperService::class.java)
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}