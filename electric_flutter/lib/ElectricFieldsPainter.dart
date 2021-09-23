import 'dart:ui';

import 'package:flutter/material.dart';

import 'Charge.dart';

typedef PictureCallback = void Function(Picture picture);

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
  final PictureCallback onPicturePainted;
  Picture? _picture;
  bool _running = false;

  void start() async {
    _running = true;
    do {
      Picture picture = await _paintDummyFrame();
      onPicturePainted(picture);
      _running = false; // ~!@
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
    Paint paint = Paint()
      ..color = Colors.greenAccent
      ..style = PaintingStyle.fill;

    for (var charge in charges) {
      Offset offset = Offset(charge.x, charge.y);
      Rect rect = Rect.fromCircle(center: offset, radius: charge.size.abs());
      canvas.drawRect(rect, paint);
    }

    Picture picture = pictureRecorder.endRecording();
    _picture = picture;
    return picture;
  }
}
