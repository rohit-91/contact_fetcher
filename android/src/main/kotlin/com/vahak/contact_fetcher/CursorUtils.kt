package com.vahak.contact_fetcher

import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.util.Log

class CursorUtils(private val contentResolver: ContentResolver) {

    private fun getSelectorString(queryString: String): String? {
        var querySelector: String? = "";
        if (queryString.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                querySelector =
                    "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE '%${queryString}%'"
            else
                querySelector =
                    "${ContactsContract.Contacts.DISPLAY_NAME} LIKE '%${queryString}%'"
        } else {
            querySelector = "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1"
        }
        return querySelector;
    }

    private fun getSortingOrder(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            return ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        else
            return ContactsContract.Contacts.DISPLAY_NAME
    }

    fun getContactsCursor(queryString: String): Cursor? {
        var contentURI = ContactsContract.Contacts.CONTENT_URI
        if (queryString.isNotEmpty()) {
            contentURI = ContactsContract.Contacts.CONTENT_URI
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
            getSelectorString(queryString),
            null,
            getSortingOrder()
        )
        Log.e("======> ","${cursor!!.count} ${queryString}")
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