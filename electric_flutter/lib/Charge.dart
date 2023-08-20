import 'dart:math';

/// Electric charge particle.
///
/// @author Moshe Waisberg
class Charge extends Point<double> {
  Charge(double x, double y, this.size) : super(x, y);

  final double size;

  @override
  String toString() {
    return "Charge($x, $y, $size)";
  }
}
