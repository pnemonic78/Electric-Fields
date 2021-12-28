import 'dart:ui' as ui;

import 'package:flutter/widgets.dart';

import 'Charge.dart';
import 'ElectricFields.dart';
import 'ElectricFieldsListener.dart';
import 'ElectricFieldsPainter.dart';

class ElectricFieldsWidget extends StatefulWidget {
  ElectricFieldsWidget({
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
  _ElectricFieldsWidgetState createState() => _ElectricFieldsWidgetState();
}

class _ElectricFieldsWidgetState extends State<ElectricFieldsWidget>
    implements ElectricFields, ElectricFieldsListener {
  _ElectricFieldsWidgetState() : super();

  static const double sameChargeDistance = 40;
  static const double sameChargeDistanceSquared =
      sameChargeDistance * sameChargeDistance;

  List<Charge> _charges = <Charge>[];
  ElectricFieldsPainter? _painter;
  ui.Image? _image;

  double _measuredWidthDiff = 0;
  double _measuredHeightDiff = 0;
  DateTime _timeTapDown = DateTime.now();

  @override
  void initState() {
    super.initState();
    final initialCharges = widget.initialCharges;
    if (initialCharges != null) {
      _charges = [...initialCharges];
      initialCharges.clear();
    }
    start();
  }

  @override
  void didUpdateWidget(covariant ElectricFieldsWidget oldWidget) {
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
    if (charges.length < ElectricFieldsWidget.MAX_CHARGES) {
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
    final lastIndex = charges.length - 1;
    int chargeNearest = -1;
    double dx;
    double dy;
    double d;
    double dMin = double.maxFinite;
    Charge charge;

    for (var i = 0; i < lastIndex; i++) {
      charge = charges[i];
      dx = x - charge.x;
      dy = y - charge.y;
      d = (dx * dx) + (dy * dy);
      if ((d <= sameChargeDistanceSquared) && (d < dMin)) {
        chargeNearest = i;
        dMin = d;
      }
    }

    return chargeNearest;
  }

  @override
  bool invertCharge(double x, double y) {
    final charges = _charges;
    var position = _findChargeIndex(x, y);
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
      width: widget.width.toInt(),
      height: widget.height.toInt(),
      charges: _charges,
      onImagePainted: _onImagePainted,
    );
    _painter = painter;
    painter.start(startDelay: delay);
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
    final imageWidget = RawImage(
      key: Key(_image?.hashCode.toString() ?? "0"),
      image: _image,
    );

    final gestureWidget = GestureDetector(
      onTapDown: _onTapDown,
      onTapUp: _onTapUp,
      child: imageWidget,
    );

    return gestureWidget;
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
    return false;
  }

  @override
  bool onChargeScaleBegin(ElectricFields view, Charge charge) {
    return false;
  }

  @override
  bool onChargeScaleEnd(ElectricFields view, Charge charge) {
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
  void onRenderFieldFinished(ElectricFields view, ui.Image image) {
    clear();
    widget.listener?.onRenderFieldFinished(view, image);
  }

  @override
  void onRenderFieldStarted(ElectricFields view) {
    widget.listener?.onRenderFieldStarted(view);
  }

  void _onImagePainted(ui.Image? image) {
    if (image != null) {
      setState(() {
        _image = image;
      });
    } else {
      final imagePrevious = _image;
      if (imagePrevious != null) onRenderFieldFinished(this, imagePrevious);
    }
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
}
