import 'package:contact_fetcher/contact.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'contact_fetcher_method_channel.dart';

abstract class ContactFetcherPlatform extends PlatformInterface {
  /// Constructs a ContactFetcherPlatform.
  ContactFetcherPlatform() : super(token: _token);

  static final Object _token = Object();

  static ContactFetcherPlatform _instance = MethodChannelContactFetcher();

  /// The default instance of [ContactFetcherPlatform] to use.
  ///
  /// Defaults to [MethodChannelContactFetcher].
  static ContactFetcherPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ContactFetcherPlatform] when
  /// they register themselves.
  static set instance(ContactFetcherPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<List<Contact>> getAllContact({int limit = 10}) {
    throw UnimplementedError('getAllContact() has not been implemented.');
  }
}
