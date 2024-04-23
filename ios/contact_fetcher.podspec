#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint contact_fetcher.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'contact_fetcher'
  s.version          = '0.0.2'
  s.summary          = 'A flutter plugin for fetching the contacts from mobile device'
  s.description      = <<-DESC
A flutter plugin for fetching the contacts from mobile device
                       DESC
  s.homepage         = 'https://github.com/rohit-91/contact_fetcher'
  s.license          = { :type => 'BSD', :file => '../LICENSE' }
  s.author           = { 'Manish' => 'manish.kummar21@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '12.0'
  s.swift_version = '5.0'
end
