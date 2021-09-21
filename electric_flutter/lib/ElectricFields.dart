import 'Charge.dart';

/// Electric fields view for drawing.
///
/// @author Moshe Waisberg
class ElectricFields {
  Charge? findCharge(int x, int y) {}

  bool invertCharge(int x, int y) {
    return false;
  }

  bool addCharge(Charge charge) {
    return false;
  }

  /// Clear the charges.
  void clear() {}

  /// Start the task.
  /// @param delay the start delay, in milliseconds.
  void start({int delay = 0}) {}

  /// Stop the task.
  void stop() {}

  /// Restart the task with modified charges.
  ///
  /// @param delay the start delay, in milliseconds.
  void restart({int delay = 0}) {
    stop();
    start(delay: delay);
  }
}
