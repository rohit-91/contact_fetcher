import 'dart:convert';

import 'package:contact_fetcher/contact.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'contact_fetcher_platform_interface.dart';

/// An implementation of [ContactFetcherPlatform] that uses method channels.
class MethodChannelContactFetcher extends ContactFetcherPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('contact_fetcher');

  @override
  Future<List<Contact>> getAllContact() async {
    final String? contactsData =
        await methodChannel.invokeMethod<String?>('get_all_contact');
    List<Contact> contacts = [];
    if ((contactsData ?? "").isNotEmpty) {
      List<dynamic> list = jsonDecode(contactsData!);
      for (var element in list) {
        contacts.add(Contact(
            id: element['id'],
            name: element['name'],
            phoneNumbers: (element['phone_numbers'] as List<dynamic>)
                .map((e) => e.toString())
                .toList()));
      }
    }
    return contacts;
  }
}
