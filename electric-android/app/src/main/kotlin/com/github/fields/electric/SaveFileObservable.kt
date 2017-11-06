/*
 * Copyright 2017, Moshe Waisberg
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
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Observable to save a bitmap to a file.
 *
 * @author Moshe Waisberg
 */
class SaveFileObservable(val context: Context, val bitmap: Bitmap) : Observable<Uri>(), Disposable {

    private lateinit var task: SaveFileTask

    override fun subscribeActual(observer: Observer<in Uri>) {
        val d = SaveFileTask(context, bitmap, observer)
        task = d
        observer.onSubscribe(d)
        d.run()
    }

    override fun isDisposed(): Boolean {
        return task.isDisposed
    }

    override fun dispose() {
        task.dispose()
    }
}