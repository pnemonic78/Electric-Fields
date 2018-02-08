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

<<<<<<< HEAD
import Foundation

=======
>>>>>>> develop
/**
 * Electric fields event listener.
 *
 * @author Moshe Waisberg
 */
<<<<<<< HEAD
protocol  ElectricFieldsListener {
//    func onChargeAdded(view: ElectricFieldsView, charge: Charge)
//    
//    func onChargeInverted(view: ElectricFieldsView, charge: Charge)
//    
//    func onChargeScaleBegin(view: ElectricFieldsView, charge: Charge) -> Boolean
//    
//    func onChargeScale(view: ElectricFieldsView, charge: Charge) -> Boolean
//    
//    func onChargeScaleEnd(view: ElectricFieldsView, charge: Charge) -> Boolean
//    
//    func onRenderFieldClicked(view: ElectricFieldsView, x: Int, y: Int, size: Double) -> Boolean
//    
//    func onRenderFieldStarted(view: ElectricFieldsView)
//    
//    func onRenderFieldFinished(view: ElectricFieldsView)
//    
//    func onRenderFieldCancelled(view: ElectricFieldsView)
=======
protocol ElectricFieldsListener {
    
    func onChargeAdded(view: ElectricFields, charge: Charge)
    
    func onChargeInverted(view: ElectricFields, charge: Charge)
    
    func onChargeScaleBegin(view: ElectricFields, charge: Charge) -> Bool
    
    func onChargeScale(view: ElectricFields, charge: Charge) -> Bool
    
    func onChargeScaleEnd(view: ElectricFields, charge: Charge) -> Bool
    
    func onRenderFieldClicked(view: ElectricFields, x: Int, y: Int, size: Double) -> Bool
    
    func onRenderFieldStarted(view: ElectricFields)
    
    func onRenderFieldFinished(view: ElectricFields)
    
    func onRenderFieldCancelled(view: ElectricFields)
>>>>>>> develop
}
