import 'Charge.dart';

/// Electric fields view for drawing.
///
/// @author Moshe Waisberg
class ElectricFields {
  Charge? findCharge(double x, double y) {}

  bool invertCharge(double x, double y) {
    return false;
  }

  bool addCharge(Charge charge) {
    return false;
  }

  bool addChargeDetails(double x, double y, double size) {
    return addCharge(Charge(x, y, size));
  }

  /// Clear the charges.
  void clear() {}

  /// Start the task.
  /// @param delay the start delay, in milliseconds.
  void start({int delay = 0}) async {}

  /// Stop the task.
  void stop() {}

  /// Restart the task with modified charges.
  ///
  /// @param delay the start delay, in milliseconds.
  void restart({int delay = 0}) async {
    stop();
    start(delay: delay);
  }
}
