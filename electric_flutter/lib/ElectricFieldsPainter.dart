import 'dart:ui';

import 'package:flutter/material.dart';

import 'Charge.dart';

typedef PictureCallback = void Function(Picture picture);

class ElectricFieldsPainter {
  ElectricFieldsPainter(
      {required this.width,
      required this.height,
      required this.charges,
      required this.callback})
      : assert(width > 0),
        assert(height > 0);

  final double width;
  final double height;
  final List<Charge> charges;
  final PictureCallback callback;
  Picture? _picture;
  bool _running = false;

  void start() async {
    _running = true;
    Picture picture = await _paintDummyFrame();
    callback(picture);
  }

  void cancel() {
    _running = false;
  }

  Future<Picture> _paintDummyFrame() async {
    Picture? pictureOld = _picture;
    PictureRecorder pictureRecorder = PictureRecorder();
    Canvas canvas = Canvas(pictureRecorder);
    canvas.drawColor(Colors.pinkAccent, BlendMode.srcOver); //~!@
    if (pictureOld != null) {
      canvas.drawPicture(pictureOld);
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
