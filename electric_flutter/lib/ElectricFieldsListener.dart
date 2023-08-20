import 'dart:ui';

import 'Charge.dart';
import 'ElectricFields.dart';

/// Electric fields event listener.
///
/// @author Moshe Waisberg
class ElectricFieldsListener {
  void onChargeAdded(ElectricFields view, Charge charge) {}

  void onChargeInverted(ElectricFields view, Charge charge) {}

  bool onChargeScaleBegin(ElectricFields view, Charge charge) {
    return false;
  }

  bool onChargeScale(ElectricFields view, Charge charge) {
    return false;
  }

  bool onChargeScaleEnd(ElectricFields view, Charge charge) {
    return false;
  }

  void onChargesCleared(ElectricFields view) {}

  bool onRenderFieldClicked(
    ElectricFields view,
    double x,
    double y,
    double size,
  ) {
    return false;
  }

  void onRenderFieldStarted(ElectricFields view) {}

  void onRenderFieldFinished(ElectricFields view, Image image) {}

  void onRenderFieldCancelled(ElectricFields view) {}
}
