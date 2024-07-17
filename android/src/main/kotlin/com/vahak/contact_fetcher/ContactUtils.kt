package com.vahak.contact_fetcher

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class ContactUtils(private var contentResolver: ContentResolver) {

    @SuppressLint("Range")
    fun fetchContactByPage(pageNumber: Int, pageLength: Int): ArrayList<JSONObject> {
        val contactList = ArrayList<JSONObject>();
        val startIndex = (pageNumber * pageLength).coerceAtLeast(1) - 1
        val contactCursor = CursorUtils(contentResolver).getContactsCursor("");
        if (contactCursor != null && contactCursor.count > 0) {
            contactCursor.moveToPosition(startIndex)
            contactList.addAll(bindDataFromCursor(contactCursor, pageLength))
            contactCursor.close()
        }
        return contactList;
    }

    fun fetchContactByName(queryString: String): ArrayList<JSONObject> {
        val contactList = ArrayList<JSONObject>();
        val contactCursor = CursorUtils(contentResolver).getContactsCursor("")
        if (contactCursor != null && contactCursor.count > 0) {
            contactCursor.moveToFirst()
            contactList.addAll(bindDataFromCursor(contactCursor, 20))
            contactCursor.close()
        }
        return contactList;
    }

    @SuppressLint("Range")
    private fun fetchPhoneDataFromCursor(cursor: Cursor?): JSONArray {
        val phoneNumberList = JSONArray()
        while (cursor!!.moveToNext()) {
            phoneNumberList.put(
                cursor.getString(
                    cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                )
            )
        }
        cursor.close()
        return phoneNumberList;
    }

    @SuppressLint("Range")
    private fun bindDataFromCursor(
        cursor: Cursor,
        pageLength: Int
    ): ArrayList<JSONObject> {
        var count = 0
        val contactList = ArrayList<JSONObject>()
        do {
            val contactObject = JSONObject()
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
            val phoneNumbers =
                fetchPhoneDataFromCursor(CursorUtils(contentResolver).getPhoneCursor(id))
            if (name.isNotEmpty() && phoneNumbers.length() > 0) {
                contactObject.put("id", id)
                contactObject.put("name", name)
                contactObject.put("phone_numbers", phoneNumbers)
                val bytes: ByteArray? = extractImageFromCursor(cursor)
                if (bytes != null) {
                    contactObject.put("photo", bytes.toList())
                }
                contactList.add(contactObject)
            }
            ++count
        } while (cursor.moveToNext() && count < pageLength)
        return contactList
    }

    @SuppressLint("Recycle")
    private fun extractImageFromCursor(cursor: Cursor): ByteArray? {
        var imageBytes: ByteArray? = null
        val columnIndex: Int = cursor.getColumnIndex(
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        if (columnIndex == -1) {
            return null
        }
        val imageUri: String = cursor.getString(
            columnIndex
        ) ?: return null
        try {
            val fis: InputStream? = contentResolver.openInputStream(Uri.parse(imageUri))
            imageBytes = fis?.readBytes()!!
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageBytes
    }
}