import 'dart:convert';
import 'dart:io';

import 'package:contact_fetcher/contact.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'contact_fetcher_platform_interface.dart';

/// An implementation of [ContactFetcherPlatform] that uses method channels.
class MethodChannelContactFetcher extends ContactFetcherPlatform {
  /// The method channel used to interact with the native platform.
  final List<Contact> contacts = [];
  int _pageNumber = 0;
  @visibleForTesting
  final methodChannel = const MethodChannel('contact_fetcher');

  @override
  Future<List<Contact>> getAllContact({int limit = 10}) async {
    final String? contactsData = await methodChannel.invokeMethod<String?>(
        'get_all_contact', {"limit": limit, "page_number": _pageNumber++});
    if ((contactsData ?? "").isNotEmpty) {
      List<dynamic> list = jsonDecode(contactsData!);
      for (var element in list) {
        Uint8List? bytes;
        if (element['photo'] != null) {
          if (Platform.isAndroid) {
            bytes = Uint8List.fromList(
                (jsonDecode(element['photo']) as List).cast<int>());
          } else if (Platform.isIOS) {
            bytes = Uint8List.fromList(element['photo'].cast<int>());
          }
        }
        contacts.add(Contact(
            id: element['id'],
            name: element['name'],
            photo: bytes,
            phoneNumbers: (element['phone_numbers'] as List<dynamic>)
                .map((e) => e.toString())
                .toList()));
      }
    }
    return contacts;
  }
}
