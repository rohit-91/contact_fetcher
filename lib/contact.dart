import 'package:flutter/foundation.dart';

class Contact {
  String id;
  String name;
  Uint8List? photo;
  List<String> phoneNumbers;

  Contact(
      {required this.id,
      required this.name,
      required this.phoneNumbers,
      required this.photo});
}
