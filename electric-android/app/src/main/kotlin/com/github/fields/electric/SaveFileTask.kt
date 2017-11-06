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
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Task to save a bitmap to a file.
 *
 * @author Moshe Waisberg
 */
class SaveFileTask(val context: Context, val bitmap: Bitmap, val observer: Observer<in Uri>) : MainThreadDisposable() {

    private val TAG = "SaveFileTask"

    private val IMAGE_MIME = "image/png"
    private val SCHEME_FILE = "file"

    private val timestampFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)

    fun run() {
        val folderPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val folder = File(folderPictures, context.getString(R.string.app_folder_pictures))
        folder.mkdirs()
        val file = File(folder, generateFileName())

        var url: Uri? = null
        var out: OutputStream? = null
        val mutex = Object()
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            Log.i(TAG, "save success: " + file)
            url = Uri.fromFile(file)
        } catch (e: IOException) {
            Log.e(TAG, "save failed: " + file, e)
            observer.onError(e)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ignore: Exception) {
                }
            }
        }
        if (url != null) {
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(IMAGE_MIME), { path: String, uri: Uri? ->
                if ((uri != null) && !SCHEME_FILE.equals(uri.scheme)) {
                    url = uri
                    observer.onNext(url)
                }
                synchronized(mutex) {
                    mutex.notify()
                }
            })
            synchronized(mutex) {
                mutex.wait()
            }
        }

        if (!isDisposed) {
            observer.onComplete()
        }
    }

    override fun onDispose() {
    }

    fun generateFileName(): String {
        return "ef-" + timestampFormat.format(Date()) + ".png"
    }
}
