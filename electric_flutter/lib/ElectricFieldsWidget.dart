import 'dart:math';
import 'dart:ui';

import 'package:electric_flutter/Charge.dart';
import 'package:electric_flutter/ElectricFields.dart';
import 'package:electric_flutter/ui/PictureWidget.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'ElectricFieldsListener.dart';

class ElectricFieldsWidget extends StatefulWidget {
  ElectricFieldsWidget(
      {Key? key, required this.width, required this.height, this.listener})
      : assert(width > 0),
        assert(height > 0),
        super(key: key);

  final double width;
  final double height;
  final ElectricFieldsListener? listener;

  @override
  _ElectricFieldsWidgetState createState() => _ElectricFieldsWidgetState();
}

class _ElectricFieldsWidgetState extends State<ElectricFieldsWidget>
    implements ElectricFields {
  static const int MIN_CHARGES = 2;
  static const int MAX_CHARGES = 10;

  static const int int64MaxValue = 0x7FFFFFFFFFFFFFFF;
  static const int sameChargeDistance = 20; // ~32dp

  final List<Charge> _charges = <Charge>[];
  Picture? _picture;

  @override
  bool addCharge(Charge charge) {
    if (_charges.length < MAX_CHARGES) {
      setState(() {
        _charges.add(charge);
      });
      widget.listener?.onChargeAdded(this, charge);
      return true;
    }
    return false;
  }

  @override
  void clear() {
    setState(() {
      _charges.clear();
    });
  }

  @override
  Charge? findCharge(int x, int y) {
    int indexNearest = _findChargeIndex(x, y);
    if (indexNearest >= 0) {
      return _charges[indexNearest];
    }
    return null;
  }

  int _findChargeIndex(int x, int y) {
    final length = _charges.length - 1;
    int chargeNearest = -1;
    int dx;
    int dy;
    int d;
    int dMin = int64MaxValue;
    Charge charge;

    for (var i = 0; i < length; i++) {
      charge = _charges[i];
      dx = x - charge.x;
      dy = y - charge.y;
      d = (dx * dx) + (dy * dy);
      if ((d <= sameChargeDistance) && (d < dMin)) {
        chargeNearest = i;
        dMin = d;
      }
    }

    return chargeNearest;
  }

  @override
  bool invertCharge(int x, int y) {
    final position = _findChargeIndex(x, y);
    if (position >= 0) {
      Charge charge = _charges[position];
      Charge chargeInverted = Charge(charge.x, charge.y, -charge.size);
      setState(() {
        _charges[position] = chargeInverted;
      });
      widget.listener?.onChargeInverted(this, chargeInverted);
      return true;
    }
    return false;
  }

  @override
  void restart({int delay = 0}) {
    stop();
    start(delay: delay);
  }

  @override
  void start({int delay = 0}) {
    // TODO: implement start
  }

  @override
  void stop() {
    // TODO: implement stop
  }

  @override
  Widget build(BuildContext context) {
    final width = widget.width;
    final height = widget.height;

    return PictureWidget(
      _getPicture(width, height),
      width: width,
      height: height,
    );
  }

  Picture _getPicture(double width, double height) {
    Picture? pictureOld = _picture;
    PictureRecorder pictureRecorder = PictureRecorder();
    Canvas canvas = Canvas(pictureRecorder);
    if (pictureOld != null) {
      canvas.drawPicture(pictureOld);
    }
    final rnd = Random();
    Rect rect = Rect.fromLTWH(rnd.nextDouble() * width,
        rnd.nextDouble() * height, width / 2, height / 2);
    Paint paint = Paint()
      ..color = Colors.red
      ..style = PaintingStyle.fill;
    canvas.drawRect(rect, paint);
    Picture picture = pictureRecorder.endRecording();
    _picture = picture;
    return picture;
  }
}
