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
  final ScrollController _controller = ScrollController();
  int _pageNumber = 0;

  @override
  void initState() {
    super.initState();
    _controller.addListener(() async {
      if (_controller.position.pixels == _controller.position.maxScrollExtent) {
        updated();
      }
    });
    updated();
  }

  updated() async {
    try {
      List<Contact> contacts = await _contactFetcherPlugin.getAllContact(
          limit: 10, pageNumber: _pageNumber);
      _contacts.addAll(contacts);
      _pageNumber++;
    } on PlatformException {
      _contacts = <Contact>[];
    }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('Contact fetcher (${_contacts.length})')),
        body: ListView.builder(
            key: Key("keyyyy"),
            controller: _controller,
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
                          if (_contacts[index].photo != null)
                            Image.memory(_contacts[index].photo!,
                                width: 40, height: 40),
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
