package com.vahak.contact_fetcher

import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract

class CursorUtils(private val contentResolver: ContentResolver) {

    private fun getSelectorString(queryString: String): String? {
        var querySelector: String? = "";
        if (queryString.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                querySelector =
                    "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ${queryString}"
            else
                querySelector =
                    "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ${queryString}"
        } else {
            querySelector = null
        }
        return querySelector;
    }

    private fun getSortingOrder(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            return ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        else
            return ContactsContract.Contacts.DISPLAY_NAME + " ASC"
    }

    fun getContactsCursor(queryString: String): Cursor? {
        var contentURI = ContactsContract.Contacts.CONTENT_URI
        if (queryString.isNotEmpty()) {
            contentURI = ContactsContract.Contacts.CONTENT_FILTER_URI
        }
        val cursor = contentResolver.query(
            contentURI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            ),
            null,
            null,
            getSortingOrder()
        )
        return cursor;
    }

    fun getPhoneCursor(contactId: String): Cursor? {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )
        return phoneCursor
    }
}