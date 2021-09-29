import 'dart:math';
import 'dart:ui';

import 'package:electric_flutter/ElectricFields.dart';
import 'package:electric_flutter/ElectricFieldsListener.dart';
import 'package:electric_flutter/ElectricFieldsWidget.dart';
import 'package:flutter/material.dart' hide Image;
import 'package:flutter/widgets.dart' hide Image;

import 'Charge.dart';
import 'SaveFileTask.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage>
    implements ElectricFieldsListener {
  static const color_action_bar = Color(0x20000000);

  ElectricFieldsWidget? _electricFieldsWidget;
  List<Charge> _charges = <Charge>[];
  final _random = Random();
  Picture? _picture;

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final mediaWidth = media.size.width;
    final mediaHeight = media.size.height;

    final fieldWidth = mediaWidth;
    final fieldHeight = mediaHeight;

    final menuItemRandom = IconButton(
      icon: Icon(Icons.shuffle),
      tooltip: "Randomise",
      onPressed: () => _randomise(fieldWidth, fieldHeight),
    );

    final menuItemSave = IconButton(
      icon: Icon(Icons.save),
      tooltip: "Save to file",
      onPressed: () => _saveToFile(fieldWidth, fieldHeight),
    );

    _electricFieldsWidget = ElectricFieldsWidget(
      width: fieldWidth,
      height: fieldHeight,
      charges: _charges,
      listener: this,
    );

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: color_action_bar,
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
        actions: [
          menuItemRandom,
          menuItemSave,
        ],
      ),
      body: _electricFieldsWidget,
    );
  }

  void _randomise(double width, double height) async {
    int w = width.toInt();
    int h = height.toInt();
    final count = _nextIntInRange(
        ElectricFieldsWidget.MIN_CHARGES, ElectricFieldsWidget.MAX_CHARGES);
    final List<Charge> charges = <Charge>[];
    for (var i = 0; i < count; i++) {
      Charge charge = Charge(_random.nextDouble() * w, _random.nextDouble() * h,
          _nextDoubleInRange(-20.0, 20.0));
      charges.add(charge);
    }
    setState(() {
      _charges = charges;
    });
  }

  int _nextIntInRange(int start, int end) =>
      start + _random.nextInt(end - start);

  double _nextDoubleInRange(double start, double end) =>
      _random.nextDouble() * (end - start) + start;

  void _saveToFile(double width, double height) async {
    final widget = _electricFieldsWidget;
    if (widget == null) return;
    final picture = _picture;
    if (picture == null) return null;
    final task = SaveFileTask();
    task.savePicture(picture, width.toInt(), height.toInt());
  }

  @override
  void onChargeAdded(ElectricFields view, Charge charge) {}

  @override
  void onChargeInverted(ElectricFields view, Charge charge) {}

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
  void onRenderFieldCancelled(ElectricFields view) {}

  @override
  bool onRenderFieldClicked(ElectricFields view, int x, int y, double size) {
    return false;
  }

  @override
  void onRenderFieldFinished(ElectricFields view, Picture picture) {
    _picture = picture;
  }

  @override
  void onRenderFieldStarted(ElectricFields view) {}
}
