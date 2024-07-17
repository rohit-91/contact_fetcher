import 'package:contact_fetcher/contact.dart';
import 'package:contact_fetcher/contact_fetcher.dart';
import 'package:flutter/cupertino.dart';
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
  final TextEditingController _textEditingController = TextEditingController();
  int _pageNumber = 0;
  bool isRequested = false;

  @override
  void initState() {
    super.initState();
    _controller.addListener(() async {
      if (_controller.position.pixels == _controller.position.maxScrollExtent &&
          _textEditingController.text.isEmpty) {
        updated();
      }
    });

    _textEditingController.addListener(() {
      if (_textEditingController.text.isNotEmpty) {
        searchByText(_textEditingController.text);
      } else {
        _pageNumber = 0;
        updated();
      }
    });
    updated();
  }

  updated() async {
    if (!isRequested) {
      isRequested = true;
      try {
        List<Contact> contacts = await _contactFetcherPlugin.getAllContact(
            limit: 10, pageNumber: _pageNumber);
        _contacts.addAll(contacts);
        _pageNumber++;
      } on PlatformException {
        _contacts = <Contact>[];
      }

      setState(() {
        isRequested = false;
      });
    }
  }

  void searchByText(String value) {
    if (!isRequested) {
      _contactFetcherPlugin.searchContact(queryString: value).then((contacts) {
        _contacts.clear();
        _contacts.addAll(contacts);
        setState(() {
          isRequested = false;
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('Contact fetcher (${_contacts.length})')),
        body: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Column(
            children: [
              Container(
                margin: const EdgeInsets.only(top: 8, bottom: 8),
                padding: const EdgeInsets.symmetric(horizontal: 16),
                decoration: BoxDecoration(
                    border: Border.all(color: Colors.black.withOpacity(0.7)),
                    borderRadius: const BorderRadius.all(Radius.circular(28)),
                    color: Colors.grey.withOpacity(0.34)),
                child: TextFormField(
                    controller: _textEditingController,
                    autofocus: true,
                    cursorColor: Colors.white,
                    cursorErrorColor: Colors.red,
                    decoration:
                        const InputDecoration(border: InputBorder.none)),
              ),
              Expanded(
                child: ListView.builder(
                    controller: _controller,
                    itemCount: _contacts.length,
                    physics: const AlwaysScrollableScrollPhysics(),
                    itemBuilder: (BuildContext context, int index) {
                      return card(_contacts[index]);
                    }),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget card(Contact contact) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Center(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (contact.photo != null)
                Image.memory(contact.photo!, width: 40, height: 40),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(left: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text("NAME: ${contact.name}"),
                      ...List.generate(contact.phoneNumbers.length,
                          (phoneIndex) {
                        return phoneNumberWidget(
                            contact.phoneNumbers[phoneIndex], phoneIndex);
                      })
                    ],
                  ),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }

  Widget phoneNumberWidget(String number, int index) {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: Text("PHONE ${index + 1} : $number"),
    );
  }
}
