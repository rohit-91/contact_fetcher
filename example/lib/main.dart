import 'dart:async';

import 'package:contact_fetcher/contact.dart';
import 'package:contact_fetcher/contact_fetcher.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<Contact> _contacts = [];
  final _contactFetcherPlugin = ContactFetcher();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    List<Contact>? contacts;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      contacts = await _contactFetcherPlugin.getAllContact();
    } on PlatformException {
      contacts = <Contact>[];
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _contacts = contacts ?? <Contact>[];
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Contact fetcher')),
        body: ListView.builder(
            itemCount: _contacts.length,
            itemBuilder: (BuildContext context, int index) {
              return Padding(
                padding: const EdgeInsets.all(8.0),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Center(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text("NAME: ${_contacts[index].name}"),
                          ListView.builder(
                              physics: const NeverScrollableScrollPhysics(),
                              shrinkWrap: true,
                              itemCount: _contacts[index].phoneNumbers.length,
                              itemBuilder: (context, phoneIndex) {
                                return Padding(
                                  padding: const EdgeInsets.only(top: 8),
                                  child: Text(
                                      "PHONE ${phoneIndex + 1} : ${_contacts[index].phoneNumbers[phoneIndex]}"),
                                );
                              })
                        ],
                      ),
                    ),
                  ),
                ),
              );
            }),
      ),
    );
  }
}
