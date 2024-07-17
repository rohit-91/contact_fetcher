import 'package:contact_fetcher/contact.dart';

import 'contact_fetcher_platform_interface.dart';

class ContactFetcher {
  Future<List<Contact>> getAllContact({int limit = 10, int pageNumber = 0}) {
    return ContactFetcherPlatform.instance
        .getAllContact(limit: limit, pageNumber: pageNumber);
  }

  Future<List<Contact>> searchContact({String queryString = ""}) {
    return ContactFetcherPlatform.instance
        .searchContact(queryString: queryString);
  }
}
