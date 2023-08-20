import 'dart:ui';

import 'package:electric_flutter/ui/RenderPicture.dart';
import 'package:flutter/widgets.dart';

class RawPicture extends LeafRenderObjectWidget {
  const RawPicture(this.picture, this.width, this.height,
      {Key? key, bool dispose = true})
      : _dispose = dispose,
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
  RenderObject createRenderObject(BuildContext context) {
    return RenderPicture(picture, width, height, dispose: _dispose);
  }
}
