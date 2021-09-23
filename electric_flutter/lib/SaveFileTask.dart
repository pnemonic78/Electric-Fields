import 'dart:io';
import 'dart:ui';

import 'package:path_provider/path_provider.dart';

/// Task to save a bitmap to a file.
///
/// @author Moshe Waisberg
class SaveFileTask {
  static const IMAGE_MIME = "image/png";

  static const IMAGE_EXT = ".png";
  static const SCHEME_FILE = "file";

  final app_folder_pictures = "Electric Fields";

  Future<File?> savePicture(Picture picture, int width, int height) async {
    final image = await picture.toImage(width.toInt(), height.toInt());
    return saveImage(image);
  }

  Future<File?> saveImage(Image image) async {
    final byteData = await image.toByteData(format: ImageByteFormat.png);
    final bytes = byteData!.buffer.asUint8List();

    final directories =
        await getExternalStorageDirectories(type: StorageDirectory.pictures);
    final folderPictures = directories!.first;
    final folder =
        await Directory(folderPictures.path + "/" + app_folder_pictures)
            .create(recursive: true);
    final folderPath = folder.path;
    final filename = _generateFileName();
    final file = File(folderPath + "/" + filename);
    return file.writeAsBytes(bytes);
  }

  String _generateFileName() {
    final now = DateTime.now();
    String y = _fourDigits(now.year);
    String m = _twoDigits(now.month);
    String d = _twoDigits(now.day);
    String h = _twoDigits(now.hour);
    String min = _twoDigits(now.minute);
    String sec = _twoDigits(now.second);
    return "ef-$y$m$d-$h$min$sec$IMAGE_EXT";
  }

  static String _fourDigits(int n) {
    int absN = n.abs();
    String sign = n < 0 ? "-" : "";
    if (absN >= 1000) return "$n";
    if (absN >= 100) return "${sign}0$absN";
    if (absN >= 10) return "${sign}00$absN";
    return "${sign}000$absN";
  }

  static String _twoDigits(int n) {
    if (n >= 10) return "$n";
    return "0$n";
  }
}
