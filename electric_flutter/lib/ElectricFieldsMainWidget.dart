import 'dart:ui';

import 'package:electric_flutter/Charge.dart';
import 'package:electric_flutter/ElectricFields.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart' hide Image;

import 'ElectricFieldsListener.dart';
import 'ElectricFieldsPainter.dart';
import 'ElectricFieldsWidget.dart';

class ElectricFieldsMainWidget extends StatefulWidget {
  ElectricFieldsMainWidget({
    Key? key,
    required this.width,
    required this.height,
    this.listener,
    this.initialCharges,
  })  : assert(width > 0),
        assert(height > 0),
        super(key: key);

  static const int MIN_CHARGES = 2;
  static const int MAX_CHARGES = 10;

  final double width;
  final double height;
  final ElectricFieldsListener? listener;
  final List<Charge>? initialCharges;

  @override
  _ElectricFieldsMainWidgetState createState() =>
      _ElectricFieldsMainWidgetState();
}

class _ElectricFieldsMainWidgetState extends State<ElectricFieldsMainWidget>
    implements ElectricFields, ElectricFieldsListener {
  _ElectricFieldsMainWidgetState() : super();

  static const int sameChargeDistance = 20; // ~32dp

  List<Charge> _charges = <Charge>[];
  ElectricFieldsPainter? _painter;

  double _measuredWidthDiff = 0;
  double _measuredHeightDiff = 0;
  DateTime _timeTapDown = DateTime.now();

  @override
  void initState() {
    super.initState();
    final initialCharges = widget.initialCharges;
    if ((initialCharges != null) && initialCharges.isNotEmpty) {
      _charges = [...initialCharges];
      initialCharges.clear();
    }
    start();
  }

  @override
  void didUpdateWidget(covariant ElectricFieldsMainWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    final initialCharges = widget.initialCharges;
    if ((initialCharges != null) && initialCharges.isNotEmpty) {
      _charges = [...initialCharges];
      initialCharges.clear();
      restart();
    }
  }

  @override
  bool addCharge(Charge charge) {
    final charges = _charges;
    if (charges.length < ElectricFieldsMainWidget.MAX_CHARGES) {
      charges.add(charge);
      onChargeAdded(this, charge);
      return true;
    }
    return false;
  }

  @override
  bool addChargeDetails(double x, double y, double size) {
    return addCharge(Charge(x, y, size));
  }

  @override
  void clear() {
    _charges = [];
    onChargesCleared(this);
  }

  @override
  Charge? findCharge(double x, double y) {
    int indexNearest = _findChargeIndex(x, y);
    if (indexNearest >= 0) {
      return _charges[indexNearest];
    }
    return null;
  }

  int _findChargeIndex(double x, double y) {
    final charges = _charges;
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
  bool invertCharge(double x, double y) {
    final charges = _charges;
    final position = _findChargeIndex(x, y);
    if (position >= 0) {
      Charge charge = charges[position];
      Charge chargeInverted = Charge(charge.x, charge.y, -charge.size);
      charges[position] = chargeInverted;
      onChargeInverted(this, chargeInverted);
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
      charges: _charges,
      onPicturePainted: _onPicturePainted,
    );
    setState(() {
      _painter = painter;
    });
    painter.start();
    onRenderFieldStarted(this);
  }

  @override
  void stop() {
    _painter?.cancel();
    _painter = null;
    onRenderFieldCancelled(this);
  }

  @override
  void dispose() {
    stop();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final width = widget.width;
    final height = widget.height;

    final fieldsWidget = ElectricFieldsWidget(
      width: width,
      height: height,
      painter: _painter!,
      listener: this,
    );

    final gestureWidget = GestureDetector(
      onTapDown: _onTapDown,
      onTapUp: _onTapUp,
      child: fieldsWidget,
    );

    return gestureWidget;
  }

  void _onPicturePainted(Picture picture) {
    onRenderFieldFinished(this, picture);
  }

  void _onTapDown(TapDownDetails details) {
    _timeTapDown = DateTime.now();
  }

  void _onTapUp(TapUpDetails details) {
    final x = details.localPosition.dx - _measuredWidthDiff;
    final y = details.localPosition.dy - _measuredHeightDiff;
    final duration = DateTime.now().difference(_timeTapDown).inMilliseconds;
    final size = 1.0 + (duration / 20);
    onRenderFieldClicked(this, x, y, size);
  }

  @override
  void onChargeAdded(ElectricFields view, Charge charge) {
    widget.listener?.onChargeAdded(view, charge);
  }

  @override
  void onChargeInverted(ElectricFields view, Charge charge) {
    widget.listener?.onChargeInverted(view, charge);
  }

  @override
  bool onChargeScale(ElectricFields view, Charge charge) {
    // TODO: implement onChargeScale
    return false;
  }

  @override
  bool onChargeScaleBegin(ElectricFields view, Charge charge) {
    // TODO: implement onChargeScaleBegin
    return false;
  }

  @override
  bool onChargeScaleEnd(ElectricFields view, Charge charge) {
    // TODO: implement onChargeScaleEnd
    return false;
  }

  @override
  void onChargesCleared(ElectricFields view) {
    widget.listener?.onChargesCleared(view);
  }

  @override
  void onRenderFieldCancelled(ElectricFields view) {
    widget.listener?.onRenderFieldCancelled(view);
  }

  @override
  bool onRenderFieldClicked(
      ElectricFields view, double x, double y, double size) {
    if ((view == this) &&
        (view.invertCharge(x, y) || view.addChargeDetails(x, y, size))) {
      widget.listener?.onRenderFieldClicked(view, x, y, size);
      view.restart();
      return true;
    }
    return false;
  }

  @override
  void onRenderFieldFinished(ElectricFields view, Picture picture) {
    widget.listener?.onRenderFieldFinished(view, picture);
  }

  @override
  void onRenderFieldStarted(ElectricFields view) {
    widget.listener?.onRenderFieldStarted(view);
  }
}
