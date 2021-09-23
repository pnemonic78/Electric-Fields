import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class RenderPicture extends RenderBox {
  RenderPicture(Picture? picture, double? width, double? height,
      {bool dispose = true})
      : _picture = picture,
        _width = width,
        _height = height,
        _dispose = dispose;

  Picture? get picture => _picture;
  Picture? _picture;
  bool _dispose = true;

  /// If non-null, requires the image to have this width.
  ///
  /// If null, the image will pick a size that best preserves its intrinsic
  /// aspect ratio.
  double? get width => _width;
  double? _width;

  set width(double? value) {
    if (value == _width) return;
    _width = value;
    markNeedsLayout();
  }

  /// If non-null, require the image to have this height.
  ///
  /// If null, the image will pick a size that best preserves its intrinsic
  /// aspect ratio.
  double? get height => _height;
  double? _height;

  set height(double? value) {
    if (value == _height) return;
    _height = value;
    markNeedsLayout();
  }

  double _scale = 1.0;

  /// Find a size for the render image within the given constraints.
  ///
  ///  - The dimensions of the RenderPicture must fit within the constraints.
  ///  - The aspect ratio of the RenderPicture matches the intrinsic aspect
  ///    ratio of the image.
  ///  - The RenderPicture's dimension are maximal subject to being smaller than
  ///    the intrinsic size of the image.
  Size _sizeForConstraints(BoxConstraints constraints) {
    // Folds the given |width| and |height| into |constraints| so they can all
    // be treated uniformly.
    constraints = BoxConstraints.tightFor(
      width: _width,
      height: _height,
    ).enforce(constraints);

    if (_picture == null) return constraints.smallest;

    return constraints.constrainSizeAndAttemptToPreserveAspectRatio(Size(
      width ?? 0.0 / _scale,
      height ?? 0.0 / _scale,
    ));
  }

  @override
  double computeMinIntrinsicWidth(double height) {
    assert(height >= 0.0);
    if (_width == null && _height == null) return 0.0;
    return _sizeForConstraints(BoxConstraints.tightForFinite(height: height))
        .width;
  }

  @override
  double computeMaxIntrinsicWidth(double height) {
    assert(height >= 0.0);
    return _sizeForConstraints(BoxConstraints.tightForFinite(height: height))
        .width;
  }

  @override
  double computeMinIntrinsicHeight(double width) {
    assert(width >= 0.0);
    if (_width == null && _height == null) return 0.0;
    return _sizeForConstraints(BoxConstraints.tightForFinite(width: width))
        .height;
  }

  @override
  double computeMaxIntrinsicHeight(double width) {
    assert(width >= 0.0);
    return _sizeForConstraints(BoxConstraints.tightForFinite(width: width))
        .height;
  }

  @override
  bool hitTestSelf(Offset position) => true;

  @override
  Size computeDryLayout(BoxConstraints constraints) {
    return _sizeForConstraints(constraints);
  }

  @override
  void performLayout() {
    size = _sizeForConstraints(constraints);
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    final picture = _picture;
    if (picture == null) return;
    final Canvas canvas = context.canvas;
    _paintPicture(canvas, offset, picture);
  }

  void _paintPicture(Canvas canvas, Offset offset, Picture picture) {
    final clip = offset & size;
    canvas.save();
    canvas.clipRect(clip);
    canvas.translate(offset.dx, offset.dy);
    canvas.drawPicture(picture);
    canvas.restore();
  }

  @override
  void dispose() {
    if (_dispose) _picture?.dispose();
    _picture = null;
    super.dispose();
  }
}
