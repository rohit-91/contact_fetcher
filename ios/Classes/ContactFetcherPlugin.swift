import Contacts
import ContactsUI
import Flutter
import Foundation
import UIKit

public class ContactFetcherPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "contact_fetcher", binaryMessenger: registrar.messenger())
        let instance = ContactFetcherPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "get_all_contact":
            result(encodeContacts(contactList:fetchContacts()))
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func fetchContacts()->[CNContact] {
        let contactStore = CNContactStore()
        let keysToFetch = [
            CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
            CNContactPhoneNumbersKey,
            CNContactEmailAddressesKey,
            CNContactThumbnailImageDataKey,
            CNContactImageDataAvailableKey,
            CNContactImageDataKey] as [Any]
        
        var allContainers: [CNContainer] = []
        do {
            allContainers = try contactStore.containers(matching: nil)
        } catch {
            print("Error fetching containers")
        }
        
        var results: [CNContact] = []
        
        for container in allContainers {
            let fetchPredicate = CNContact.predicateForContactsInContainer(withIdentifier: container.identifier)
            
            do {
                let containerResults = try contactStore.unifiedContacts(matching: fetchPredicate, keysToFetch: keysToFetch as! [CNKeyDescriptor])
                results.append(contentsOf: containerResults)
            } catch {
                print("Error fetching containers")
            }
        }
        return results;
    }
    
    private func encodeContacts(contactList:[CNContact])->String{
        var result:[[String:Any]] = [];
        contactList.forEach { contact in
            var item:[String:Any]=[:];
            item["id"]=contact.identifier;
            item["name"]=""+contact.namePrefix+" "+contact.givenName+" "+contact.middleName+" "+contact.familyName+" "+contact.nameSuffix;
            var numbers:[String]=[];
            contact.phoneNumbers.forEach { phoneNumber in
                numbers.append(phoneNumber.value.stringValue);
            }
            item["phone_numbers"]=numbers;
            if let imageData = contact.imageData {
                let contactImage = UIImage(data: imageData)
                item["photo"] = self.convertImageToIntegerList(image: contactImage)
            }
            result.append(item);
        }
        var encodedBytes=Data();
        do{
            encodedBytes = try JSONSerialization.data(withJSONObject:result, options: JSONSerialization.WritingOptions())
        }catch{
            print("Json parsing failed for contacts");
        }
        return String(data:encodedBytes,encoding: .utf8)!;
    }
    
    func convertImageToIntegerList(image: UIImage?) -> [Int]? {
        guard let image = image else { return nil }
        
        if let imageData = image.jpegData(compressionQuality: 1.0) {
            let byteArray = [UInt8](imageData)
            let integerList = byteArray.map { Int($0) }
            return integerList
        }
        
        return nil
    }
}
