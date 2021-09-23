import 'dart:ui';

import 'package:electric_flutter/Charge.dart';
import 'package:electric_flutter/ElectricFields.dart';
import 'package:electric_flutter/ui/PictureWidget.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import 'ElectricFieldsListener.dart';
import 'ElectricFieldsPainter.dart';

class ElectricFieldsWidget extends StatefulWidget {
  ElectricFieldsWidget(
      {Key? key,
      required this.width,
      required this.height,
      required this.charges,
      this.listener})
      : assert(width > 0),
        assert(height > 0),
        super(key: key);

  static const int MIN_CHARGES = 2;
  static const int MAX_CHARGES = 10;

  final double width;
  final double height;
  final List<Charge> charges;

  final ElectricFieldsListener? listener;

  @override
  _ElectricFieldsWidgetState createState() => _ElectricFieldsWidgetState();
}

class _ElectricFieldsWidgetState extends State<ElectricFieldsWidget>
    implements ElectricFields {
  static const int sameChargeDistance = 20; // ~32dp

  Picture? _picture;
  ElectricFieldsPainter? _painter;

  @override
  void didUpdateWidget(covariant ElectricFieldsWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    restart();
  }

  @override
  bool addCharge(Charge charge) {
    final charges = widget.charges;
    if (charges.length < ElectricFieldsWidget.MAX_CHARGES) {
      charges.add(charge);
      widget.listener?.onChargeAdded(this, charge);
      restart();
      return true;
    }
    return false;
  }

  @override
  void clear() {
    widget.charges.clear();
    restart();
  }

  @override
  Charge? findCharge(int x, int y) {
    int indexNearest = _findChargeIndex(x, y);
    if (indexNearest >= 0) {
      return widget.charges[indexNearest];
    }
    return null;
  }

  int _findChargeIndex(int x, int y) {
    final charges = widget.charges;
    final length = charges.length - 1;
    int chargeNearest = -1;
    double dx;
    double dy;
    double d;
    double dMin = double.maxFinite;
    Charge charge;

    for (var i = 0; i < length; i++) {
      charge = charges[i];
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
    final charges = widget.charges;
    final position = _findChargeIndex(x, y);
    if (position >= 0) {
      Charge charge = charges[position];
      Charge chargeInverted = Charge(charge.x, charge.y, -charge.size);
      charges[position] = chargeInverted;
      widget.listener?.onChargeInverted(this, chargeInverted);
      restart();
      return true;
    }
    return false;
  }

  @override
  void restart({int delay = 0}) async {
    stop();
    start(delay: delay);
  }

  @override
  void start({int delay = 0}) async {
    ElectricFieldsPainter painter = ElectricFieldsPainter(
      width: widget.width,
      height: widget.height,
      charges: widget.charges,
      onPicturePainted: _picturePainted,
    );
    _painter = painter;
    painter.start();
  }

  @override
  void stop() {
    _painter?.cancel();
    _painter = null;
    _picture?.dispose();
    _picture = null;
  }

  @override
  Widget build(BuildContext context) {
    final width = widget.width;
    final height = widget.height;

    return PictureWidget(
      picture: _picture,
      width: width,
      height: height,
      key: UniqueKey(),
      dispose: false,
    );
  }

  void _picturePainted(Picture picture) {
    setState(() {
      _picture = picture;
    });
  }
}
