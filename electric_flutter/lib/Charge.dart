import 'dart:math';

/// Electric charge particle.
///
/// @author Moshe Waisberg
class Charge extends Point<int> {
  Charge(int x, int y, this.size) : super(x, y);

  final double size;

  @override
  String toString() {
    return "Charge($x, $y, $size)";
  }
}
