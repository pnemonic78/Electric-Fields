import 'dart:math';
import 'dart:ui';

import 'package:electric_flutter/ElectricFields.dart';
import 'package:electric_flutter/ElectricFieldsListener.dart';
import 'package:flutter/material.dart' hide Image;
import 'package:share_plus/share_plus.dart';

import 'Charge.dart';
import 'ElectricFieldsWidget.dart';
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
  List<Charge>? _charges;
  final _random = Random();
  Image? _image;

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final mediaWidth = media.size.width;
    final mediaHeight = media.size.height;
    final mediaRatio = media.devicePixelRatio;

    final fieldWidth = mediaWidth * mediaRatio;
    final fieldHeight = mediaHeight * mediaRatio;

    final menuItemRandom = IconButton(
      icon: Icon(Icons.shuffle),
      tooltip: "Randomise",
      onPressed: () => _randomise(fieldWidth, fieldHeight),
    );

    final menuItemShare = IconButton(
      icon: Icon(Icons.share),
      tooltip: "Share",
      onPressed: () => _shareImage(),
    );

    _electricFieldsWidget = ElectricFieldsWidget(
      width: fieldWidth,
      height: fieldHeight,
      listener: this,
      initialCharges: _charges,
    );
    _charges = null;

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: color_action_bar,
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
        actions: [
          menuItemRandom,
          menuItemShare,
        ],
      ),
      body: _electricFieldsWidget,
    );
  }

  void _randomise(double width, double height) async {
    final count = _nextIntInRange(
      ElectricFieldsWidget.MIN_CHARGES,
      ElectricFieldsWidget.MAX_CHARGES,
    );
    final List<Charge> charges = <Charge>[];
    for (var i = 0; i < count; i++) {
      Charge charge = Charge(
        _random.nextDouble() * width,
        _random.nextDouble() * height,
        _nextDoubleInRange(-20.0, 20.0),
      );
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

  void _shareImage() async {
    final image = _image;
    if (image == null) return;
    final task = SaveFileTask();
    final file = await task.saveImage(image);
    if (file == null) return;
    final path = file.path;
    print('saved to $path');
    await Share.shareFiles(
      [path],
      mimeTypes: [SaveFileTask.IMAGE_MIME],
      subject: "Share image",
      text: "Share Electric Fields Image",
    );
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
  void onChargesCleared(ElectricFields view) {}

  @override
  void onRenderFieldCancelled(ElectricFields view) {}

  @override
  bool onRenderFieldClicked(
      ElectricFields view, double x, double y, double size) {
    return false;
  }

  @override
  void onRenderFieldFinished(ElectricFields view, Image image) {
    _image = image;
  }

  @override
  void onRenderFieldStarted(ElectricFields view) {}

  @override
  void dispose() {
    _image = null;
    super.dispose();
  }
}
