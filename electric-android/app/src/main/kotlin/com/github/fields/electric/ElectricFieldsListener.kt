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

/**
 * Electric fields event listener.
 *
 * @author Moshe Waisberg
 */
interface ElectricFieldsListener {

    fun onChargeAdded(view: ElectricFieldsView, charge: Charge)

    fun onChargeInverted(view: ElectricFieldsView, charge: Charge)

    fun onChargeScaleBegin(view: ElectricFieldsView, charge: Charge): Boolean

    fun onChargeScale(view: ElectricFieldsView, charge: Charge): Boolean

    fun onChargeScaleEnd(view: ElectricFieldsView, charge: Charge): Boolean

    fun onRenderFieldClicked(view: ElectricFieldsView, x: Int, y: Int, size: Double): Boolean

    fun onRenderFieldStarted(view: ElectricFieldsView)

    fun onRenderFieldFinished(view: ElectricFieldsView)

    fun onRenderFieldCancelled(view: ElectricFieldsView)
}
