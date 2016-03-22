/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kludgenics.cgmlogger.app.viewmodel

import android.databinding.Observable
import android.databinding.PropertyChangeRegistry


interface DataBindingObservable : Observable {
    var mCallbacks: PropertyChangeRegistry?

    @Synchronized override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        if (mCallbacks == null) {
            mCallbacks = PropertyChangeRegistry()
        }
        mCallbacks!!.add(callback)
    }

    @Synchronized override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        if (mCallbacks != null) {
            mCallbacks!!.remove(callback)
        }
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    @Synchronized fun notifyChange() {
        if (mCallbacks != null) {
            mCallbacks!!.notifyCallbacks(this, 0, null)
        }
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.

     * @param fieldId The generated BR id for the Bindable field.
     */
    @Synchronized
    fun notifyPropertyChanged(fieldId: Int) {
        if (mCallbacks != null) {
            mCallbacks!!.notifyCallbacks(this, fieldId, null)
        }
    }
}
