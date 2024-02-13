import 'package:contact_fetcher/contact.dart';
import 'package:contact_fetcher/contact_fetcher.dart';
import 'package:contact_fetcher/contact_fetcher_method_channel.dart';
import 'package:contact_fetcher/contact_fetcher_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockContactFetcherPlatform
    with MockPlatformInterfaceMixin
    implements ContactFetcherPlatform {
  @override
  Future<List<Contact>> getAllContact() {
    return Future.value(<Contact>[]);
  }
}

void main() {
  final ContactFetcherPlatform initialPlatform =
      ContactFetcherPlatform.instance;

  test('$MethodChannelContactFetcher is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelContactFetcher>());
  });

  test('getAllContact', () async {
    ContactFetcher contactFetcherPlugin = ContactFetcher();
    MockContactFetcherPlatform fakePlatform = MockContactFetcherPlatform();
    ContactFetcherPlatform.instance = fakePlatform;

    expect(await contactFetcherPlugin.getAllContact(), <Contact>[]);
  });
}
