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
      required this.onPicturePainted})
      : assert(width > 0),
        assert(height > 0);

  final double width;
  final double height;
  final List<Charge> charges;
  final Set<PictureCallback> onPicturePainted;

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
      Picture picture = await _paintDummyFrame();
      onPicturePainted.forEach((callback) => callback(picture));
      _run();
    } while (_running);
  }

  void cancel() {
    _running = false;
    _picture = null;
  }

  Future<Picture> _paintDummyFrame() async {
    Picture? pictureOld = _picture;
    PictureRecorder pictureRecorder = PictureRecorder();
    Canvas canvas = Canvas(pictureRecorder);
    if (pictureOld != null) {
      canvas.drawPicture(pictureOld);
      pictureOld.dispose();
    }
    canvas.drawColor(Colors.pink, BlendMode.src);
    Paint paint = _paint..color = Colors.blue;

    for (var charge in charges) {
      Offset offset = Offset(charge.x, charge.y);
      Rect rect = Rect.fromCircle(center: offset, radius: charge.size.abs());
      canvas.drawRect(rect, paint);
    }

    Picture picture = pictureRecorder.endRecording();
    _picture = picture;
    return picture;
  }

  void _run() {
    // val w = bitmap.width
    // val h = bitmap.height
    // var size = max(w, h)
    //
    // var shifts = 0
    // while (size > 1) {
    //   size = size ushr 1
    //   shifts++
    // }
    //
    // // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
    // var resolution2 = 1 shl shifts
    // var resolution = resolution2
    //
    // val canvas = Canvas(bitmap)
    // canvas.drawColor(WHITE)
    // plot(charges, canvas, 0, 0, resolution, resolution, density)
    //
    // var x1: Int
    // var y1: Int
    // var x2: Int
    // var y2: Int
    //
    // loop@ do {
    // y1 = 0
    // y2 = resolution
    //
    // do {
    // x1 = 0
    // x2 = resolution
    //
    // do {
    // plot(charges, canvas, x1, y2, resolution, resolution, density)
    // plot(charges, canvas, x2, y1, resolution, resolution, density)
    // plot(charges, canvas, x2, y2, resolution, resolution, density)
    //
    // x1 += resolution2
    // x2 += resolution2
    // } while ((x1 < w) && !isDisposed)
    //
    // if (isDisposed) {
    // break@loop
    // }
    // onPicturePainted(picture);
    //
    // y1 += resolution2
    // y2 += resolution2
    // } while (y1 < h)
    //
    // resolution2 = resolution
    // resolution = resolution2 shr 1
    // } while ((resolution >= 1) && !isDisposed)

    _running = false;
    // if (!isDisposed) {
    // onPicturePainted(picture);
    // observer.onComplete()
    // }
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
}
