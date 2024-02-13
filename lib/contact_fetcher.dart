import 'package:contact_fetcher/contact.dart';

import 'contact_fetcher_platform_interface.dart';

class ContactFetcher {
  Future<List<Contact>> getAllContact() {
    return ContactFetcherPlatform.instance.getAllContact();
  }
}
