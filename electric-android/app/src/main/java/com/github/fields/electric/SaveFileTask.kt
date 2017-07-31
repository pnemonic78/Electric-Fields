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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Task to save a bitmap to a file.

 * @author Moshe Waisberg
 */
class SaveFileTask(val context: Context) : AsyncTask<Bitmap, File, Uri>() {

    val TAG = "SaveFileTask"

    val REQUEST_APP = 0x0466 // "APP"
    val REQUEST_VIEW = 0x7133 // "VIEW"

    val ID_NOTIFY = 0x5473 // "SAVE"

    val IMAGE_MIME = "image/png"

    val timestampFormat: DateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)

    var bitmap: Bitmap? = null
    var builder: Notification.Builder? = null

    override fun doInBackground(vararg params: Bitmap): Uri? {
        val folderPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(folderPictures, context.getString(R.string.app_folder_pictures))
        folder.mkdirs()
        val file = File(folder, generateFileName())

        val bitmap = params[0]
        this.bitmap = bitmap

        val res = context.resources
        val iconWidth = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
        val iconHeight = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
        val largeIcon = Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, false)

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(context, REQUEST_APP, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder = Notification.Builder(context)
                .setContentTitle(context.getText(R.string.saving_title))
                .setContentText(context.getText(R.string.saving_text))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.stat_notify)
                .setLargeIcon(largeIcon)
                .setAutoCancel(true)
                .setOngoing(true)

        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder!!.build()
        } else {
            notification = builder!!.notification
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ID_NOTIFY, notification)

        var url: Uri? = null
        var out: OutputStream? = null
        val mutex = Object()
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            out = null
            Log.i(TAG, "save success: " + file)
            url = Uri.fromFile(file)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(IMAGE_MIME), { path: String, uri: Uri? ->
                if ((uri != null) && !"file".equals(uri.scheme)) {
                    url = uri
                }
                synchronized(mutex) {
                    mutex.notify()
                }
            })
            synchronized(mutex) {
                mutex.wait()
            }
        } catch (e: IOException) {
            Log.e(TAG, "save failed: " + file, e)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ignore: Exception) {
                }
            }
        }

        return url
    }

    override fun onPostExecute(file: Uri?) {
        builder!!.setOngoing(false)

        if (file != null && bitmap != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(file, IMAGE_MIME)
            val pendingIntent = PendingIntent.getActivity(context, REQUEST_VIEW, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            builder!!.setContentTitle(context.getText(R.string.saved_title))
                    .setContentText(context.getText(R.string.saved_text))
                    .setContentIntent(pendingIntent)
        } else {
            builder!!.setContentText(context.getText(R.string.save_failed))
        }

        val notification: Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder!!.build()
        } else {
            notification = builder!!.notification
        }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ID_NOTIFY, notification)
    }

    override fun onCancelled(file: Uri?) {
        super.onCancelled(file)
        if (file == null) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(ID_NOTIFY)
        }
    }

    fun generateFileName(): String {
        return "ef-" + timestampFormat.format(Date()) + ".png"
    }
}
