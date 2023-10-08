import 'dart:ui';

import 'package:electric_flutter/ui/RawPicture.dart';
import 'package:flutter/widgets.dart';

class PictureWidget extends StatelessWidget {
  const PictureWidget({
    Key? key,
    this.picture,
    this.width,
    this.height,
    bool dispose = true,
  })  : _dispose = dispose,
        super(key: key);

  final Picture? picture;

  /// If non-null, require the image to have this width.
  ///
  /// If null, the image will pick a size that best preserves its intrinsic
  /// aspect ratio.
  final double? width;

  /// If non-null, require the image to have this height.
  ///
  /// If null, the image will pick a size that best preserves its intrinsic
  /// aspect ratio.
  final double? height;

  final bool _dispose;

  @override
  Widget build(BuildContext context) {
    return RawPicture(picture, width, height, key: key, dispose: _dispose);
  }
}
