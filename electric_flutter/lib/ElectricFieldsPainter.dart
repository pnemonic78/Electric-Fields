import 'dart:async';
import 'dart:isolate';
import 'dart:math';
import 'dart:ui';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart' as w;
import 'package:image/image.dart' as img;

import 'Charge.dart';

typedef ImageCallback = void Function(Image? image);

const DEFAULT_DENSITY = 1000.0;
const DEFAULT_HUES = 360.0;

const white = 0xFFFFFFFF;

class ElectricFieldsPainter {
  ElectricFieldsPainter({
    required this.width,
    required this.height,
    required this.charges,
    required this.onImagePainted,
  })  : assert(width > 0),
        assert(height > 0);

  final int width;
  final int height;
  final List<Charge> charges;
  final SendPort onImagePainted;

  final density = DEFAULT_DENSITY;
  final hues = DEFAULT_HUES;

  img.Image? _image;

  img.Image? get image => _image;

  bool _running = false;

  List<double> _hsv = [0, 1, 1];

  double get saturation => _hsv[1];

  set saturation(double s) => _hsv[1] = s;

  double get brightness => _hsv[2];

  set brightness(double b) => _hsv[2] = b;

  void start({int startDelay = 0}) async {
    if (startDelay > 0) {
      await Future.delayed(Duration(milliseconds: startDelay));
    }
    _run();
  }

  void cancel() {
    _running = false;
    _image = null;
  }

  void _run() async {
    _running = true;

    final w = this.width;
    final h = this.height;
    var size = max(w, h);

    var shifts = 0;
    while (size > 1) {
      size = size >> 1;
      shifts++;
    }

    // Make "resolution2" a power of 2, so that "resolution" is always divisible by 2.
    var resolution2 = (1 << shifts).toDouble();
    var resolution = resolution2;

    img.Image canvas = img.Image(w, h);
    canvas.fill(white);
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

        y1 += resolution2;
        y2 += resolution2;
      } while ((y1 < h) && _running);

      if (_running) {
        notifyImagePainted(canvas);
      }

      resolution2 = resolution;
      resolution = resolution2 / 2;
    } while ((resolution >= 1) && _running);

    if (_running) {
      _running = false;
      notifyImagePainted(canvas);
      notifyImagePainted(null);
    }
  }

  void _plot(List<Charge> charges, img.Image canvas, double x, double y,
      double w, double h, double zoom) async {
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

    int color = _mapColor(v, zoom);
    int x1 = x.toInt();
    int y1 = y.toInt();
    int x2 = (x + w).toInt();
    int y2 = (y + h).toInt();

    img.fillRect(canvas, x1, y1, x2, y2, color);
  }

  int _mapColor(double z, double density) {
    if (z.isInfinite || z.isNaN) {
      return white;
    }
    _hsv[0] = (z * density) % hues;
    return w.HSVColor.fromAHSV(1.0, _hsv[0], _hsv[1], _hsv[2]).toColor().value;
  }

  void notifyImagePainted(img.Image? image) {
    if (image != null) _image = image;
    onImagePainted.send(image);
  }

  static void paintWithPainter(ElectricFieldsPainter painter) async {
    await compute(_paintIsolated, painter);
  }

  static Future<ElectricFieldsPainter> paint({
    required int width,
    required int height,
    required List<Charge> charges,
    required ReceivePort port,
  }) async {
    ElectricFieldsPainter painter = ElectricFieldsPainter(
      width: width,
      height: height,
      charges: charges,
      onImagePainted: port.sendPort,
    );
    paintWithPainter(painter);
    return painter;
  }

  static void _paintIsolated(ElectricFieldsPainter painter) async {
    painter.start();
  }
}
