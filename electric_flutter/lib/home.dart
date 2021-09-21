import 'dart:math';

import 'package:electric_flutter/ElectricFieldsWidget.dart';
import 'package:flutter/material.dart';

import 'Charge.dart';

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  ElectricFieldsWidget? _electricFieldsWidget;
  List<Charge> _charges = <Charge>[];
  final _random = Random();

  static const color_action_bar = Color(0x20000000);

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

    _electricFieldsWidget = ElectricFieldsWidget(
      width: fieldWidth,
      height: fieldHeight,
      charges: _charges,
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
        ],
      ),
      body: _electricFieldsWidget,
    );
  }

  void _randomise(double width, double height) {
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
}
