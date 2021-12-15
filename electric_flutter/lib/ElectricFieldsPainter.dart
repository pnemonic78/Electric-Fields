import 'dart:collection';
import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';

import 'Charge.dart';

typedef PictureCallback = void Function(Picture picture);

const DEFAULT_DENSITY = 1000.0;
const DEFAULT_HUES = 360.0;

class ElectricFieldsPainter {
  ElectricFieldsPainter(
      {required this.width,
      required this.height,
      required this.charges,
      required PictureCallback onPicturePainted})
      : assert(width > 0),
        assert(height > 0) {
    addOnPicturePainted(onPicturePainted);
  }

  final double width;
  final double height;
  final List<Charge> charges;
  final Set<PictureCallback> _onPicturePaintedCallbacks = HashSet<PictureCallback>();

  final density = DEFAULT_DENSITY;
  final hues = DEFAULT_HUES;

  Picture? _picture;
  bool _running = false;

  Paint _paint = Paint()
    ..strokeCap = StrokeCap.square
    ..strokeWidth = 1
    ..style = PaintingStyle.fill;

  List<double> _hsv = [0, 1, 1];

  double get saturation => _hsv[1];

  set saturation(double s) => _hsv[1] = s;

  double get brightness => _hsv[2];

  set brightness(double b) => _hsv[2] = b;

  void start() async {
    _running = true;
    do {
      _run();
    } while (_running);
  }

  void cancel() {
    _running = false;
    _picture = null;
  }

  void _run() async {
    final w = this.width;
    final h = this.height;
    var size = max(w, h);

    var shifts = 0;
    while (size > 1) {
      size = size / 2;
      shifts++;
    }

    // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
    var resolution2 = (1 << shifts).toDouble();
    var resolution = resolution2;

    Picture picture;
    Picture? pictureOld = _picture;
    PictureRecorder pictureRecorder = PictureRecorder();
    Canvas canvas = Canvas(pictureRecorder);
    canvas.drawColor(Colors.white, BlendMode.src);
    _plot(charges, canvas, 0, 0, resolution, resolution, density);

    double x1;
    double y1;
    double x2;
    double y2;

    do {
      y1 = 0;
      y2 = resolution;

      do {
        x1 = 0;
        x2 = resolution;

        do {
          _plot(charges, canvas, x1, y2, resolution, resolution, density);
          _plot(charges, canvas, x2, y1, resolution, resolution, density);
          _plot(charges, canvas, x2, y2, resolution, resolution, density);

          x1 += resolution2;
          x2 += resolution2;
        } while ((x1 < w) && _running);

        if (!_running) {
          break;
        }
        pictureOld?.dispose();
        picture = pictureRecorder.endRecording();
        notifyPicturePainted(picture);
        pictureOld = picture;
        pictureRecorder = PictureRecorder();
        canvas = Canvas(pictureRecorder);
        canvas.drawPicture(picture);

        y1 += resolution2;
        y2 += resolution2;
      } while (y1 < h);

      resolution2 = resolution;
      resolution = resolution2 / 2;
    } while ((resolution >= 1) && _running);

    if (_running) {
      _running = false;
      picture = pictureRecorder.endRecording();
      _picture = picture;
      notifyPicturePainted(picture);
    }
    pictureOld?.dispose();
  }

  void _plot(List<Charge> charges, Canvas canvas, double x, double y, double w,
      double h, double zoom) {
    double dx;
    double dy;
    double d;
    double r;
    double v = 1.0;
    int count = charges.length;
    Charge charge;

    for (var i = 0; i < count; i++) {
      charge = charges[i];
      dx = x - charge.x;
      dy = y - charge.y;
      d = (dx * dx) + (dy * dy);
      r = sqrt(d.toDouble());
      if (r == 0.0) {
        //Force "overflow".
        v = double.maxFinite;
        break;
      }
      v += charge.size / r;
    }

    _paint.color = _mapColor(v, zoom);
    Rect rect = Rect.fromLTWH(x, y, w, h);
    canvas.drawRect(rect, _paint);
  }

  Color _mapColor(double z, double density) {
    if (z.isInfinite || z.isNaN) {
      return Colors.white;
    }
    _hsv[0] = (z * density) % hues;
    return HSVColor.fromAHSV(1.0, _hsv[0], _hsv[1], _hsv[2]).toColor();
  }

  void addOnPicturePainted(PictureCallback onPicturePainted) {
    _onPicturePaintedCallbacks.add(onPicturePainted);

    final picture = _picture;
    if (picture != null) {
      onPicturePainted(picture);
    }
  }

  void notifyPicturePainted(Picture picture) {
    _picture = picture;
    _onPicturePaintedCallbacks.forEach((callback) => callback(picture));
  }
}
