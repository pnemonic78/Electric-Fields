import 'dart:ui';

import 'package:electric_flutter/ui/PictureWidget.dart';
import 'package:flutter/widgets.dart' hide Image;

import 'ElectricFieldsListener.dart';
import 'ElectricFieldsPainter.dart';

class ElectricFieldsWidget extends StatefulWidget {
  ElectricFieldsWidget(
      {Key? key,
      required this.width,
      required this.height,
      required this.painter,
      this.listener})
      : assert(width > 0),
        assert(height > 0),
        super(key: key);

  final double width;
  final double height;
  final ElectricFieldsPainter painter;
  final ElectricFieldsListener? listener;

  @override
  _ElectricFieldsWidgetState createState() => _ElectricFieldsWidgetState();
}

class _ElectricFieldsWidgetState extends State<ElectricFieldsWidget> {
  _ElectricFieldsWidgetState() : super();

  Picture? _picture;

  @override
  void initState() {
    super.initState();
    widget.painter.addOnPicturePainted(_onPicturePainted);
  }

  @override
  void didUpdateWidget(covariant ElectricFieldsWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    widget.painter.addOnPicturePainted(_onPicturePainted);
  }

  @override
  Widget build(BuildContext context) {
    final width = widget.width;
    final height = widget.height;

    return PictureWidget(
      key: Key(_picture?.hashCode.toString() ?? "0"),
      picture: _picture,
      width: width,
      height: height,
      dispose: false,
    );
  }

  void _onPicturePainted(Picture picture) {
    setState(() {
      _picture = picture;
    });
  }
}
